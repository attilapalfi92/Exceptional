package com.attilapalf.exceptional.utils;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.attilapalf.exceptional.model.Exception;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.sql.Timestamp;


/**
 * Created by 212461305 on 2015.06.29..
 */
public class GcmMessageHandler extends IntentService {

    String message;
    private Handler handler;
    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        handler = new Handler();
    }


    // TODO: this should run on the UI thread.
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        String notificationType = extras.getString("notificationType");

        switch (notificationType) {
            case "exception":

                int typeId = Integer.parseInt(extras.getString("typeId"));
                long instanceId = Long.parseLong(extras.getString("instanceId"));
                long fromWho = Long.parseLong(extras.getString("fromWho"));
                long toWho = Long.parseLong(extras.getString("toWho"));
                double longitude = Double.parseDouble(extras.getString("longitude"));
                double latitude = Double.parseDouble(extras.getString("latitude"));
                long timeInMillis = Long.parseLong(extras.getString("timeInMillis"));

                if (!ExceptionManager.getInstance().isInitialized()) {
                    ExceptionManager.getInstance().initialize(getApplicationContext());
                }

                if (!ExceptionFactory.isInitialized()) {
                    ExceptionFactory.initialize(getApplicationContext());
                }

                Exception e = new Exception();
                e.setExceptionType(ExceptionFactory.findById(typeId));
                e.setInstanceId(instanceId);
                e.setFromWho(fromWho);
                e.setToWho(toWho);
                e.setLongitude(longitude);
                e.setLatitude(latitude);
                e.setDate(new Timestamp(timeInMillis));
                ExceptionManager.getInstance().addException(e);

                break;

            default:
                break;
        }

        message = extras.getString("title") + " " + extras.getString("body");
        showToast();
        Log.i("GCM", "Received : (" + messageType + ")  " + extras.getString("title"));

        GcmBroadcastReceiver.completeWakefulIntent(intent);

    }

    public void showToast(){
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
