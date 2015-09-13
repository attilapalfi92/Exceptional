package com.attilapalfi.exceptional.services;

import java.math.BigInteger;
import java.sql.Timestamp;

import javax.inject.Inject;

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
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.services.persistent_stores.*;
import com.attilapalfi.exceptional.ui.ShowNotificationActivity;
import com.attilapalfi.exceptional.ui.main.MainActivity;


/**
 * Created by 212461305 on 2015.06.29..
 */
public class GcmMessageHandler extends IntentService {
    private Handler handler;
    private Exception exception;
    private static int notificationIdCounter = 0;
    @Inject ExceptionInstanceManager exceptionInstanceManager;
    @Inject ExceptionTypeManager exceptionTypeManager;
    @Inject
    FriendRealm friendManager;
    @Inject MetadataStore metadataStore;

    public GcmMessageHandler( ) {
        super( "GcmMessageHandler" );
    }

    @Override
    public void onCreate( ) {
        // TODO Auto-generated method stub
        super.onCreate();
        Injector.INSTANCE.getApplicationComponent().inject( this );
        handler = new Handler( Looper.getMainLooper() );
    }

    @Override
    protected void onHandleIntent( Intent intent ) {
        Bundle extras = intent.getExtras();
        String notificationType = extras.getString( "notificationType" );
        if ( notificationType == null ) {
            GcmBroadcastReceiver.completeWakefulIntent( intent );
            return;
        }
        handleNotification( extras, notificationType );
        GcmBroadcastReceiver.completeWakefulIntent( intent );
    }

    private void handleNotification( Bundle extras, String notificationType ) {
        switch ( notificationType ) {
            case "exception":
                handleExceptionNotification( extras );
                break;
            case "friend":
                handleFriendNotification( extras );
                break;
            default:
                break;
        }
    }

    private void handleFriendNotification( Bundle extras ) {
        String fullName = extras.getString( "fullName" );
        showFriendNotification( fullName + " joined!", "Throw an exception into them face!" );
    }

    private void handleExceptionNotification( Bundle extras ) {
        parseNotificationToException( extras );
        saveDataOnMainThread( extras );
        Bundle bundle = createBundle();
        showExceptionNotification( "New exception caught!", "You have caught a(n) " + exception.getShortName(), bundle );
    }

    private void parseNotificationToException( Bundle extras ) {
        initException();
        int typeId = Integer.parseInt( extras.getString( "typeId" ) );
        exception.setExceptionType( exceptionTypeManager.findById( typeId ) );
        exception.setInstanceId( new BigInteger( extras.getString( "instanceId" ) ) );
        exception.setFromWho( extras.getString( "fromWho" ) );
        exception.setToWho( extras.getString( "toWho" ) );
        exception.setLongitude( Double.parseDouble( extras.getString( "longitude" ) ) );
        exception.setLatitude( Double.parseDouble( extras.getString( "latitude" ) ) );
        exception.setDate( new Timestamp( Long.parseLong( extras.getString( "timeInMillis" ) ) ) );
    }

    private void initException( ) {
        exception = new Exception();
    }

    @NonNull
    private Bundle createBundle( ) {
        Bundle bundle = new Bundle();
        bundle.putInt( "typeId", exception.getExceptionTypeId() );
        bundle.putString( "fromWho", exception.getFromWho().toString() );
        bundle.putDouble( "longitude", exception.getLongitude() );
        bundle.putDouble( "latitude", exception.getLatitude() );
        bundle.putLong( "timeInMillis", exception.getDate().getTime() );
        return bundle;
    }

    private void saveDataOnMainThread( final Bundle extras ) {
        handler.post( ( ) -> {
            exceptionInstanceManager.addExceptionAsync( exception );
            savePoints( extras );
        } );
    }

    private void savePoints( Bundle extras ) {
        String yourPointsString = extras.getString( "yourPoints" );
        if ( yourPointsString != null ) {
            metadataStore.setPoints( Integer.parseInt( yourPointsString ) );
        }
        String friendPointsString = extras.getString( "friendPoints" );
        if ( friendPointsString != null ) {
            friendManager.updateFriendPoints( exception.getFromWho(), Integer.parseInt( friendPointsString ) );
        }

    }

    private void showFriendNotification( String title, String text ) {
        Bundle bundle = new Bundle();
        bundle.putInt( "startPage", 1 );
        PendingIntent resultPendingIntent = createPendingIntentForFriendNotification( bundle );
        Notification notification = buildNotification( title, text, resultPendingIntent );
        notifyUser( notification );
    }

    private PendingIntent createPendingIntentForFriendNotification( Bundle bundle ) {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create( this );
        Intent resultIntent = new Intent( this, MainActivity.class );
        resultIntent.putExtras( bundle );
        stackBuilder.addParentStack( MainActivity.class ); // Adds the back stack
        stackBuilder.addNextIntent( resultIntent ); // Adds the Intent to the top of the stack
        return stackBuilder.getPendingIntent( 0, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    private Notification buildNotification( String title, String text, PendingIntent resultPendingIntent ) {
        return new NotificationCompat.Builder( this )
                .setAutoCancel( true )
                .setSmallIcon( R.drawable.logo )
                .setContentTitle( title )
                .setContentText( text )
                .setContentIntent( resultPendingIntent )
                .build();
    }

    private void notifyUser( Notification notification ) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        mNotificationManager.notify( notificationIdCounter++, notification );
    }


    private void showExceptionNotification( String title, String text, Bundle bundle ) {
        PendingIntent pendingIntent = createPendingIntentForExceptionNotification( bundle );
        Notification notification = buildNotification( title, text, pendingIntent );
        notifyUser( notification );
    }

    @NonNull
    private PendingIntent createPendingIntentForExceptionNotification( Bundle bundle ) {
        Intent resultIntent = new Intent( this, ShowNotificationActivity.class );
        resultIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        resultIntent.putExtras( bundle ); // putting data into the intent
        return PendingIntent.getActivity(  // there's no need to create an artificial back stack.
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

}
