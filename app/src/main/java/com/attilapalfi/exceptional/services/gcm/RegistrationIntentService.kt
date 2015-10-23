package com.attilapalfi.exceptional.services.gcm

import android.app.IntentService
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.rest.AppStartRestConnector
import com.google.android.gms.gcm.GcmPubSub
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.android.gms.iid.InstanceID
import java.io.IOException
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-18.
 */
public class RegistrationIntentService : IntentService(RegistrationIntentService.TAG) {
    @Inject
    lateinit var appStartRestConnector: AppStartRestConnector

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
    }

    override fun onHandleIntent(intent: Intent) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        try {
            val instanceID = InstanceID.getInstance(this)
            val token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null)
            appStartRestConnector.backendFirstAppStart(token)
            subscribeTopics(token)
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply()
        } catch (e: Exception) {
            handleError(e, sharedPreferences)
        }
    }

    private fun handleError(e: Exception, sharedPreferences: SharedPreferences) {
        Log.d(TAG, "Failed to complete token refresh", e)
        Toast.makeText(applicationContext, getString(R.string.failed_to_connect_to_gcm_servers)
                + e.message, Toast.LENGTH_LONG).show()
        sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply()
    }


    @Throws(IOException::class)
    private fun subscribeTopics(token: String) {
        val pubSub = GcmPubSub.getInstance(this)
        for (topic in TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null)
        }
    }

    companion object {
        private val TAG = "RegIntentService"
        private val TOPICS = arrayOf("global")
        private val SENT_TOKEN_TO_SERVER = "SENT_TOKEN_TO_SERVER"
    }
}