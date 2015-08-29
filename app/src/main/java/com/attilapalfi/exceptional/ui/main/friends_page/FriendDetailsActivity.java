package com.attilapalfi.exceptional.ui.main.friends_page;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.model.Friend;
import com.attilapalfi.exceptional.services.persistent_stores.FriendsManager;

import java.math.BigInteger;

public class FriendDetailsActivity extends AppCompatActivity {
    private Friend friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_details);

        getFriend();
        ImageView imageView = (ImageView) findViewById(R.id.friend_details_image);
        friend.setImageToView(imageView);
        TextView nameView = (TextView) findViewById(R.id.friend_details_name);
        nameView.setText(friend.getName());
        TextView pointsView = (TextView) findViewById(R.id.friend_details_points);
        pointsView.setText("Points: " + friend.getPoints());
    }

    public Friend getFriend() {
        if (friend == null) {
            BigInteger friendId = new BigInteger(getIntent().getStringExtra(FriendsFragment.FRIEND_ID));
            friend = FriendsManager.getInstance().findFriendById(friendId);
        }
        return friend;
    }

    public void throwExceptionClicked(View view) {

    }
}
