package com.attilapalfi.exceptional.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionInstanceManager;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionTypeManager;
import com.attilapalfi.exceptional.services.persistent_stores.FriendsManager;
import com.attilapalfi.exceptional.services.persistent_stores.MetadataStore;
import com.attilapalfi.exceptional.ui.ShowNotificationActivity;
import com.attilapalfi.exceptional.ui.main.MainActivity;

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
        handleNotification(extras, notificationType);
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void handleNotification(Bundle extras, String notificationType) {
        switch (notificationType) {
            case "exception":
                handleExceptionNotification(extras);
                break;
            case "friend":
                handleFriendNotification(extras);
                break;
            default:
                break;
        }
    }

    private void handleFriendNotification(Bundle extras) {
        String fullName = extras.getString("fullName");
        if(!FriendsManager.isInitialized()) {
            FriendsManager.initialize(getApplicationContext());
        }
        showFriendNotification(fullName + " joined!", "Throw an exception into them face!");
    }

    private void handleExceptionNotification(Bundle extras) {
        parseNotificationToException(extras);
        saveDataOnMainThread(extras);
        Bundle bundle = createBundle();
        showExceptionNotification("New exception caught!", "You have caught a(n) " + exception.getShortName(), bundle);
    }

    private void parseNotificationToException(Bundle extras) {
        initException();
        int typeId = Integer.parseInt(extras.getString("typeId"));
        exception.setExceptionType(ExceptionTypeManager.findById(typeId));
        exception.setInstanceId(new BigInteger(extras.getString("instanceId")));
        exception.setFromWho(new BigInteger(extras.getString("fromWho")));
        exception.setToWho(new BigInteger(extras.getString("toWho")));
        exception.setLongitude(Double.parseDouble(extras.getString("longitude")));
        exception.setLatitude(Double.parseDouble(extras.getString("latitude")));
        exception.setDate(new Timestamp(Long.parseLong(extras.getString("timeInMillis"))));
    }

    private void initException() {
        if (!ExceptionTypeManager.isInitialized()) {
            ExceptionTypeManager.initialize(getApplicationContext());
        }
        exception = new Exception();
    }

    @NonNull
    private Bundle createBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("typeId", exception.getExceptionTypeId());
        bundle.putString("fromWho", exception.getFromWho().toString());
        bundle.putDouble("longitude", exception.getLongitude());
        bundle.putDouble("latitude", exception.getLatitude());
        bundle.putLong("timeInMillis", exception.getDate().getTime());
        return bundle;
    }

    private void saveDataOnMainThread(final Bundle extras) {
        initServices();
        handler.post(() -> {
            ExceptionInstanceManager.addException(exception);
            savePoints(extras);
        });
    }

    private void initServices() {
        if (!ExceptionInstanceManager.isInitialized()) {
            ExceptionInstanceManager.initialize(getApplicationContext());
        }
        if (!MetadataStore.isInitialized()) {
            MetadataStore.initialize(getApplicationContext());
        }
        if (!FriendsManager.isInitialized()) {
            FriendsManager.initialize(getApplicationContext());
        }
    }

    private void savePoints(Bundle extras) {
        String yourPointsString = extras.getString("yourPoints");
        if (yourPointsString != null) {
            MetadataStore.setPoints(Integer.parseInt(yourPointsString));
        }
        String friendPointsString = extras.getString("friendPoints");
        if (friendPointsString != null) {
            FriendsManager.updateFriendPoints(exception.getFromWho(), Integer.parseInt(friendPointsString));
        }

    }

    private void showFriendNotification(String title, String text) {
        Bundle bundle = new Bundle();
        bundle.putInt("startPage", 1);
        PendingIntent resultPendingIntent = createPendingIntentForFriendNotification(bundle);
        Notification notification = buildNotification(title, text, resultPendingIntent);
        notifyUser(notification);
    }

    private PendingIntent createPendingIntentForFriendNotification(Bundle bundle) {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtras(bundle);
        stackBuilder.addParentStack(MainActivity.class); // Adds the back stack
        stackBuilder.addNextIntent(resultIntent); // Adds the Intent to the top of the stack
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Notification buildNotification(String title, String text, PendingIntent resultPendingIntent) {
        return new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(resultPendingIntent)
                .build();
    }

    private void notifyUser(Notification notification) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationIdCounter++, notification);
    }


    private void showExceptionNotification(String title, String text, Bundle bundle) {
        PendingIntent pendingIntent = createPendingIntentForExceptionNotification(bundle);
        Notification notification = buildNotification(title, text, pendingIntent);
        notifyUser(notification);
    }

    @NonNull
    private PendingIntent createPendingIntentForExceptionNotification(Bundle bundle) {
        Intent resultIntent = new Intent(this, ShowNotificationActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        resultIntent.putExtras(bundle); // putting data into the intent
        return PendingIntent.getActivity(  // there's no need to create an artificial back stack.
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

}
