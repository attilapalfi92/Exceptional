package com.attilapalf.exceptional.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.Exception;
import com.attilapalf.exceptional.services.persistent_stores.ExceptionInstanceManager;
import com.attilapalf.exceptional.services.persistent_stores.ExceptionTypeManager;
import com.attilapalf.exceptional.services.persistent_stores.MetadataStore;
import com.attilapalf.exceptional.ui.ShowNotificationActivity;
import com.attilapalf.exceptional.ui.main.MainActivity;

import java.math.BigInteger;
import java.sql.Timestamp;


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

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        String notificationType = extras.getString("notificationType");

        if (notificationType == null) {
            GcmBroadcastReceiver.completeWakefulIntent(intent);
            return;
        }

        switch (notificationType) {
            case "exception":
                parseNotificationToException(extras);
                int points = Integer.parseInt(extras.getString("points"));
                if (!MetadataStore.getInstance().isInitialized()) {
                    MetadataStore.getInstance().initialize(getApplicationContext());
                }
                MetadataStore.getInstance().setPoints(points);

                saveExceptionToStore();







                Bundle bundle = new Bundle();
                bundle.putInt("typeId", exception.getExceptionTypeId());
                bundle.putString("fromWho", exception.getFromWho().toString());
                bundle.putDouble("longitude", exception.getLongitude());
                bundle.putDouble("latitude", exception.getLatitude());
                bundle.putLong("timeInMillis", exception.getDate().getTime());

                showExceptionNotification("New exception caught!",
                        "You caught a(n) " + exception.getShortName(), bundle);

                break;

            case "friend":

                long friendId = Long.parseLong(extras.getString("friendId"));

                showFriendNotification("New friend joined!", "Throw an exception into his/her face!");

                break;
            default:
                break;
        }


        GcmBroadcastReceiver.completeWakefulIntent(intent);

    }

    private void parseNotificationToException(Bundle extras) {
        if (!ExceptionTypeManager.getInstance().isInitialized()) {
            ExceptionTypeManager.getInstance().initialize(getApplicationContext());
        }
        int typeId = Integer.parseInt(extras.getString("typeId"));
        BigInteger instanceId = new BigInteger(extras.getString("instanceId"));
        BigInteger fromWho = new BigInteger(extras.getString("fromWho"));
        BigInteger toWho = new BigInteger(extras.getString("toWho"));
        double longitude = Double.parseDouble(extras.getString("longitude"));
        double latitude = Double.parseDouble(extras.getString("latitude"));
        long timeInMillis = Long.parseLong(extras.getString("timeInMillis"));

        exception = new Exception();
        exception.setExceptionType(ExceptionTypeManager.getInstance().findById(typeId));
        exception.setInstanceId(instanceId);
        exception.setFromWho(fromWho);
        exception.setToWho(toWho);
        exception.setLongitude(longitude);
        exception.setLatitude(latitude);
        exception.setDate(new Timestamp(timeInMillis));
    }


    private void saveExceptionToStore() {
        if (!ExceptionInstanceManager.getInstance().isInitialized()) {
            ExceptionInstanceManager.getInstance().initialize(getApplicationContext());
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                ExceptionInstanceManager.getInstance().addException(exception, true);
            }
        });
    }


    private void showFriendNotification(String title, String text) {
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        Bundle bundle = new Bundle();
        bundle.putInt("startPage", 2);

        // putting data into the intent
        resultIntent.putExtras(bundle);

        // Adds the back stack
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);

        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(text);

        builder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(notificationIdCounter++, builder.build());
    }


    private void showExceptionNotification(String title, String text, Bundle bundle) {
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

}
