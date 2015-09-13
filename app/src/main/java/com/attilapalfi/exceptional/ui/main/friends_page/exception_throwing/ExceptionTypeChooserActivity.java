package com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.model.Friend;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionTypeManager;
import com.attilapalfi.exceptional.services.persistent_stores.FriendRealm;
import com.attilapalfi.exceptional.ui.main.Constants;
import com.attilapalfi.exceptional.ui.main.page_transformers.ZoomOutPageTransformer;
import io.realm.Realm;

public class ExceptionTypeChooserActivity extends AppCompatActivity {
    private ExceptionTypePagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private static Friend friend;
    @Inject ExceptionTypeManager exceptionTypeManager;
    @Inject
    FriendRealm friendsManager;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_exception_type_chooser );
        Injector.INSTANCE.getApplicationComponent().inject( this );
        setTitle();
        initFriend();
        initViewPager();
    }

    @Override
    protected void onDestroy( ) {
        super.onDestroy();
        viewPager.removeOnPageChangeListener( pagerAdapter );
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

    private void setTitle( ) {
        List<String> exceptionTypeList = new ArrayList<>( exceptionTypeManager.getExceptionTypes() );
        setTitle( exceptionTypeList.get( 0 ) );
    }

    private void initFriend( ) {
        String friendId = getIntent().getStringExtra( Constants.FRIEND_ID );
        try ( Realm realm = Realm.getInstance( getApplicationContext() ) ) {
            friend = realm.where( Friend.class ).equalTo( "id", friendId ).findFirst();
        }
    }

    private void initViewPager( ) {
        pagerAdapter = new ExceptionTypePagerAdapter( getSupportFragmentManager(), this );
        viewPager = (ViewPager) findViewById( R.id.exception_type_pager );
        viewPager.setAdapter( pagerAdapter );
        viewPager.addOnPageChangeListener( pagerAdapter );
        viewPager.setPageTransformer( true, new ZoomOutPageTransformer() );
    }

    public static Friend getFriend( ) {
        return friend;
    }
}
