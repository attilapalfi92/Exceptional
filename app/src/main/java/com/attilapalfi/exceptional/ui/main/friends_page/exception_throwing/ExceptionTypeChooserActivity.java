package com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.model.ExceptionType;
import com.attilapalfi.exceptional.model.Friend;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionTypeManager;
import com.attilapalfi.exceptional.services.persistent_stores.FriendsManager;
import com.attilapalfi.exceptional.ui.main.Constants;
import com.attilapalfi.exceptional.ui.main.page_transformers.ZoomOutPageTransformer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ExceptionTypeChooserActivity extends AppCompatActivity {
    private ExceptionTypePagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private static Friend friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception_type_chooser);
        setTitle();
        initFriend();
        initViewPager();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewPager.removeOnPageChangeListener(pagerAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setTitle() {
        List<String> exceptionTypeList = new ArrayList<>(ExceptionTypeManager.getExceptionTypes());
        setTitle(exceptionTypeList.get(0));
    }

    private void initFriend() {
        BigInteger friendId = new BigInteger(getIntent().getStringExtra(Constants.FRIEND_ID));
        friend = FriendsManager.findFriendById(friendId);
    }

    private void initViewPager() {
        pagerAdapter = new ExceptionTypePagerAdapter(getSupportFragmentManager(), this);
        viewPager = (ViewPager) findViewById(R.id.exception_type_pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(pagerAdapter);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
    }

    public static Friend getFriend() {
        return friend;
    }
}
