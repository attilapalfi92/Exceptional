package com.attilapalf.exceptional.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.ExceptionType;
import com.attilapalf.exceptional.model.Friend;
import com.attilapalf.exceptional.services.ExceptionFactory;
import com.attilapalf.exceptional.services.FriendsManager;

import java.sql.Timestamp;


public class ShowNotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_notification);

        Bundle bundle = getIntent().getExtras();
        int typeId = bundle.getInt("typeId");
        long fromWho = bundle.getLong("fromWho");
        double longitude = bundle.getDouble("longitude");
        double latitude = bundle.getDouble("latitude");
        long timeInMillis = bundle.getLong("timeInMillis");

        TextView exceptionNameView = (TextView) findViewById(R.id.fullExcetpionNameText);
        TextView exceptionDescView = (TextView) findViewById(R.id.exceptionDescriptionText);
        ImageView senderImageView = (ImageView) findViewById(R.id.exceptionSenderImage);
        TextView senderNameView = (TextView) findViewById(R.id.senderNameText);
        TextView senderPosView = (TextView) findViewById(R.id.senderPositionText);
        TextView sendDateView = (TextView) findViewById(R.id.sendDateText);

        ExceptionType exceptionType = ExceptionFactory.findById(typeId);
        exceptionNameView.setText(exceptionType.getPrefix() + "\n" + exceptionType.getShortName());
        exceptionDescView.setText(exceptionType.getDescription());
        Friend sender = FriendsManager.getInstance().findFriendById(fromWho);

        if (sender != null) {
            if (FriendsManager.getInstance().isItYourself(sender)) {
                senderNameView.setText("Yourself");
            } else {
                senderNameView.setText(sender.getName());
            }

            sender.setImageToView(senderImageView);
            senderPosView.setText(longitude + "\n" + latitude);
            Timestamp timestamp = new Timestamp(timeInMillis);
            sendDateView.setText(timestamp.toString());
        }

    }
}
