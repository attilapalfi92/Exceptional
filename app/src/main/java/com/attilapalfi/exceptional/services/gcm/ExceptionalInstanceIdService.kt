package com.attilapalfi.exceptional.services.gcm

import com.google.android.gms.iid.InstanceIDListenerService

/**
 * Created by palfi on 2015-10-18.
 */
public class ExceptionalInstanceIdService : InstanceIDListenerService() {

    // TODO: extend backend to be able to update GCM ID for a user and use it from the client
    override public fun onTokenRefresh() {
        //val intent = Intent(this, RegistrationIntentService::class.java)
        //startService(intent)
    }
}