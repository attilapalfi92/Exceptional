package com.attilapalfi.exceptional.ui.main.friends_page;

import javax.inject.Inject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.model.Friend;
import com.attilapalfi.exceptional.services.persistent_stores.ImageCache;
import com.attilapalfi.exceptional.services.persistent_stores.FriendRealm;
import com.attilapalfi.exceptional.ui.main.Constants;
import com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing.ExceptionTypeChooserActivity;
import io.realm.Realm;

public class FriendDetailsActivity extends AppCompatActivity {
    private Friend friend;
    @Inject
    FriendRealm friendsManager;
    @Inject ImageCache imageCache;

    @Override
    protected void onResume( ) {
        super.onResume();
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        Injector.INSTANCE.getApplicationComponent().inject( this );
        setContentView( R.layout.activity_friend_details );

        initFriend();
        ImageView imageView = (ImageView) findViewById( R.id.friend_details_image );
        imageCache.setImageToView( friend, imageView );
        TextView nameView = (TextView) findViewById( R.id.friend_details_name );
        nameView.setText( friend.getFirstName() + " " + friend.getLastName() );
        TextView pointsView = (TextView) findViewById( R.id.friend_details_points );
        pointsView.setText( "Points: " + friend.getPoints() );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch ( item.getItemId() ) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected( item );
        }
    }

    private void initFriend( ) {
        String friendId = getIntent().getStringExtra( Constants.FRIEND_ID );
        try ( Realm realm = Realm.getInstance( getApplicationContext() ) ) {
            friend = realm.where( Friend.class ).equalTo( "id", friendId ).findFirst();
        }
    }

    public void throwExceptionClicked( View view ) {
        Intent intent = new Intent( this, ExceptionTypeChooserActivity.class );
        intent.putExtra( Constants.FRIEND_ID, friend.getId().toString() );
        startActivity( intent );
    }

    public Friend getFriend( ) {
        if ( friend == null ) {
            initFriend();
        }
        return friend;
    }
}
