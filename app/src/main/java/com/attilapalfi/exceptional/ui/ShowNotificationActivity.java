package com.attilapalfi.exceptional.ui;

import java.math.BigInteger;
import java.sql.Timestamp;

import javax.inject.Inject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.model.ExceptionType;
import com.attilapalfi.exceptional.model.Friend;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionTypeManager;
import com.attilapalfi.exceptional.services.persistent_stores.FriendsManager;
import com.attilapalfi.exceptional.services.persistent_stores.ImageCache;


public class ShowNotificationActivity extends AppCompatActivity {
    @Inject
    ExceptionTypeManager exceptionTypeManager;
    @Inject
    FriendsManager friendsManager;
    @Inject
    ImageCache imageCache;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_show_notification );
        Injector.INSTANCE.getApplicationComponent().inject( this );

        Bundle bundle = getIntent().getExtras();
        int typeId = bundle.getInt( "typeId" );
        BigInteger fromWho = new BigInteger( bundle.getString( "fromWho" ) );
        double longitude = bundle.getDouble( "longitude" );
        double latitude = bundle.getDouble( "latitude" );
        long timeInMillis = bundle.getLong( "timeInMillis" );

        TextView exceptionNameView = (TextView) findViewById( R.id.fullExcetpionNameText );
        TextView exceptionDescView = (TextView) findViewById( R.id.exceptionDescriptionText );
        ImageView senderImageView = (ImageView) findViewById( R.id.exceptionSenderImage );
        TextView senderNameView = (TextView) findViewById( R.id.senderNameText );
        TextView senderPosView = (TextView) findViewById( R.id.senderPositionText );
        TextView sendDateView = (TextView) findViewById( R.id.sendDateText );

        ExceptionType exceptionType = exceptionTypeManager.findById( typeId );
        exceptionNameView.setText( exceptionType.getPrefix() + "\n" + exceptionType.getShortName() );
        exceptionDescView.setText( exceptionType.getDescription() );
        Friend sender = friendsManager.findFriendById( fromWho );

        if ( sender != null ) {
            if ( friendsManager.isItYourself( sender ) ) {
                senderNameView.setText( "Yourself" );
            } else {
                senderNameView.setText( sender.getFirstName() );
            }

            sender.setImageToView( senderImageView, imageCache );
            senderPosView.setText( longitude + "\n" + latitude );
            Timestamp timestamp = new Timestamp( timeInMillis );
            sendDateView.setText( timestamp.toString() );
        }

    }
}
