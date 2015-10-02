package com.attilapalfi.exceptional.services;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by 212461305 on 2015.06.29..
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive( Context context, Intent intent ) {
        ComponentName comp = new ComponentName( context.getPackageName(), GcmMessageHandler.class.getName() );
        WakefulBroadcastReceiver.startWakefulService( context, ( intent.setComponent( comp ) ) ); // Start the service, keeping the device awake while it is launching.
        setResultCode( Activity.RESULT_OK );
    }
}
