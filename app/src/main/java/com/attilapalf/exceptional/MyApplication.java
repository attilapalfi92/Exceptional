package com.attilapalf.exceptional;

import android.app.Application;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.attilapalf.exceptional.rest.BackendConnector;
import com.attilapalf.exceptional.utils.ExceptionFactory;
import com.attilapalf.exceptional.utils.ExceptionManager;
import com.attilapalf.exceptional.utils.FacebookManager;
import com.attilapalf.exceptional.utils.FriendsManager;

/**
 * Created by Attila on 2015-06-05.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ExceptionManager.initalize(getApplicationContext());
        FriendsManager friendsPreference = FriendsManager.getInstance(getApplicationContext());

        String aId = Settings.Secure.getString(getApplicationContext()
                .getContentResolver(), Settings.Secure.ANDROID_ID);

        friendsPreference.addBackendService(BackendConnector.getInstance().setAndroidId(aId));

        FacebookManager.getInstance().registerFriendListListener(friendsPreference);
        FacebookManager.getInstance().onAppStart(this);
        ExceptionFactory.initialize(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        FacebookManager.getInstance().onAppKilled();
    }
}
