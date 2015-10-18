package com.attilapalfi.exceptional.rest

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.ExceptionFactory
import com.attilapalfi.exceptional.model.Friend
import com.attilapalfi.exceptional.persistence.*
import com.attilapalfi.exceptional.rest.messages.AppStartRequest
import com.attilapalfi.exceptional.rest.messages.AppStartResponse
import com.attilapalfi.exceptional.services.gcm.RegistrationIntentService
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import java.math.BigInteger
import javax.inject.Inject

/**
 * Created by palfi on 2015-09-12.
 */
public class AppStartRestConnector {
    @Inject
    lateinit val context: Context
    @Inject
    lateinit val exceptionInstanceStore: ExceptionInstanceStore
    @Inject
    lateinit val exceptionTypeStore: ExceptionTypeStore
    @Inject
    lateinit val friendStore: FriendStore
    @Inject
    lateinit val metadataStore: MetadataStore
    @Inject
    lateinit val restInterfaceFactory: RestInterfaceFactory
    @Inject
    lateinit val questionStore: QuestionStore
    @Inject
    lateinit val exceptionFactory: ExceptionFactory
    private val appStartRestInterface by lazy { restInterfaceFactory.create(context, AppStartRestInterface::class.java) }
    public var androidId: String = ""
    private val requestBody = AppStartRequest()

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
    }

    public fun onFirstAppStart(friendList: List<Friend>, profileId: BigInteger) {
        initRequestBody(friendList, profileId)
        requestBody.deviceName = getDeviceName()
        gcmFirstAppStart()
    }

    public fun onRegularAppStart(friendList: List<Friend>, profileId: BigInteger) {
        initRequestBody(friendList, profileId)
        requestBody.exceptionVersion = metadataStore.exceptionVersion
        try {
            appStartRestInterface?.regularAppStart(requestBody, object : Callback<AppStartResponse> {
                override fun success(responseBody: AppStartResponse?, response: Response?) {
                    responseBody?.let {
                        saveCommonData(it)
                    }
                }

                override fun failure(error: RetrofitError) {
                    Toast.makeText(context, context.getString(R.string.failed_to_connect) + error.getMessage(),
                            Toast.LENGTH_LONG).show()
                }
            })

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    private fun initRequestBody(friendList: List<Friend>, profileId: BigInteger) {
        requestBody.deviceId = androidId
        requestBody.userFacebookId = profileId
        requestBody.friendsFacebookIds = friendList.map { it.id }
        requestBody.knownExceptionIds = exceptionInstanceStore.getKnownIds()
        requestBody.firstName = metadataStore.user.firstName
        requestBody.lastName = metadataStore.user.lastName
    }

    private fun gcmFirstAppStart() {
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            val intent = Intent(context, RegistrationIntentService::class.java)
            context.startService(intent);
        }
    }

    public fun backendFirstAppStart(gcmToken: String) {
        requestBody.gcmId = gcmToken

        try {
            appStartRestInterface?.firstAppStart(requestBody, object : Callback<AppStartResponse> {
                override fun success(responseBody: AppStartResponse?, response: Response?) {
                    responseBody?.let {
                        saveCommonData(it)
                        metadataStore.isFirstStartFinished = true
                    }
                }

                override fun failure(error: RetrofitError) {
                    Toast.makeText(context, context.getString(R.string.failed_to_connect_3) + error.getMessage(),
                            Toast.LENGTH_LONG).show()
                }
            })
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    private fun saveCommonData(responseBody: AppStartResponse) {
        Thread {
            saveTypesAndPoints(responseBody)
            saveMetadata(responseBody)
            saveQuestionsAndExceptions(responseBody)
        }.start()
    }

    private fun saveTypesAndPoints(responseBody: AppStartResponse) {
        if (responseBody.exceptionVersion > metadataStore.exceptionVersion) {
            exceptionTypeStore.addExceptionTypes(responseBody.exceptionTypes)
        }
        friendStore.updatePointsOfFriends(responseBody.friendsPoints)
    }

    private fun saveMetadata(responseBody: AppStartResponse) {
        metadataStore.points = responseBody.points
        metadataStore.isSubmittedThisWeek = responseBody.submittedThisWeek
        metadataStore.isVotedThisWeek = responseBody.votedThisWeek
        metadataStore.exceptionVersion = responseBody.exceptionVersion
    }

    private fun saveQuestionsAndExceptions(responseBody: AppStartResponse) {
        exceptionTypeStore.setVotedExceptionTypes(responseBody.beingVotedTypes)
        val exceptionList = responseBody.myExceptions.map { exceptionFactory.createFromWrapper(it) }
        questionStore.addUnfilteredList(exceptionList)
        exceptionInstanceStore.saveExceptionList(exceptionList)
    }

    public fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        if (model.startsWith(manufacturer)) {
            return capitalize(model)
        } else {
            return capitalize(manufacturer) + " " + model
        }
    }

    private fun capitalize(s: String?): String {
        if (s == null || s.length() == 0) {
            return ""
        }
        val first = s.charAt(0)
        if (Character.isUpperCase(first)) {
            return s
        } else {
            return Character.toUpperCase(first) + s.substring(1)
        }
    }

    private fun checkPlayServices(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance();
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                Toast.makeText(context, context.getString(R.string.install_google_services), Toast.LENGTH_LONG)
            } else {
                Toast.makeText(context, context.getString(R.string.google_services_not_found), Toast.LENGTH_LONG)
            }
            return false;
        }
        return true;
    }
}
