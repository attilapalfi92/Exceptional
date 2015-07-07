package com.attilapalf.exceptional.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.attilapalf.exceptional.model.Exception;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.sql.Timestamp;


/**
 * Created by 212461305 on 2015.06.29..
 */
public class GcmMessageHandler extends IntentService {

    private String message;
    private Handler handler;
    private Exception exception;


    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
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

                exception = new Exception();
                exception.setExceptionType(ExceptionFactory.findById(typeId));
                exception.setInstanceId(instanceId);
                exception.setFromWho(fromWho);
                exception.setToWho(toWho);
                exception.setLongitude(longitude);
                exception.setLatitude(latitude);
                exception.setDate(new Timestamp(timeInMillis));

                addException();
                message = extras.getString("title") + " " + extras.getString("body");

                break;

            default:
                message = "Something else.";
                break;
        }

        Log.i("GCM", "Received : (" + messageType + ")  " + extras.getString("title"));

        GcmBroadcastReceiver.completeWakefulIntent(intent);

    }


    public void addException() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ExceptionManager.getInstance().addException(exception, true);
            }
        });
    }

}
