package com.attilapalf.exceptional;

import android.app.Application;

import com.attilapalf.exceptional.utils.ExceptionFactory;
import com.attilapalf.exceptional.utils.ExceptionPreferences;
import com.attilapalf.exceptional.utils.FacebookManager;
import com.attilapalf.exceptional.utils.FriendsPreferences;

/**
 * Created by Attila on 2015-06-05.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FriendsPreferences friendsPreference = FriendsPreferences.getInstance(getApplicationContext());
        //friendsPreference.addAppStartHandler()
        FacebookManager.registerFriendListListener(friendsPreference);
        FacebookManager.onAppStart(this);
        ExceptionFactory.initialize(getApplicationContext());
        ExceptionPreferences.initalize(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        FacebookManager.onAppKilled();
    }
}
