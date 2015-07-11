package com.attilapalf.exceptional.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.Exception;
import com.attilapalf.exceptional.ui.ShowNotificationActivity;
import com.attilapalf.exceptional.ui.main.MainActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by 212461305 on 2015.06.29..
 */
public class GcmMessageHandler extends IntentService {

    private Handler handler;
    private Exception exception;
    private static int notificationIdCounter = 0;


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

//        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
//        String messageType = gcm.getMessageType(intent);

        String notificationType = extras.getString("notificationType");

        if (notificationType == null) {
            GcmBroadcastReceiver.completeWakefulIntent(intent);
            return;
        }

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

                Bundle bundle = new Bundle();
                bundle.putInt("typeId", typeId);
                bundle.putLong("fromWho", fromWho);
                bundle.putDouble("longitude", longitude);
                bundle.putDouble("latitude", latitude);
                bundle.putLong("timeInMillis", timeInMillis);

                showNotification("New exception caught!",
                        "You caught a(n) " + exception.getShortName(), bundle);

                break;

            default:
                break;
        }


        GcmBroadcastReceiver.completeWakefulIntent(intent);

    }


    public void showNotification(String title, String text, Bundle bundle) {
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(title)
                        .setContentText(text);


        // setting activity to start on notification click
        Intent resultIntent = new Intent(this, ShowNotificationActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // putting data into the intent
        resultIntent.putExtras(bundle);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        notificationBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(notificationIdCounter++, notificationBuilder.build());
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
