package com.attilapalf.exceptional;

import android.app.Application;
import android.provider.Settings;

import com.attilapalf.exceptional.rest.BackendConnector;
import com.attilapalf.exceptional.utils.ExceptionFactory;
import com.attilapalf.exceptional.utils.ExceptionManager;
import com.attilapalf.exceptional.utils.FacebookManager;
import com.attilapalf.exceptional.utils.FriendsManager;
import com.attilapalf.exceptional.utils.ImageCache;

/**
 * Created by Attila on 2015-06-05.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ImageCache.getInstance().initialize(getApplicationContext());

        if (!ExceptionFactory.isInitialized()) {
            ExceptionFactory.initialize(getApplicationContext());
        }
        if (!ExceptionManager.getInstance().isInitialized()) {
            ExceptionManager.getInstance().initialize(getApplicationContext());
        }
        if (!FriendsManager.getInstance().isInitialized()) {
            FriendsManager.getInstance().initialize(getApplicationContext());
        }

        String aId = Settings.Secure.getString(getApplicationContext()
                .getContentResolver(), Settings.Secure.ANDROID_ID);

        BackendConnector.init(getApplicationContext());
        FriendsManager.getInstance().addBackendService(BackendConnector.getInstance().setAndroidId(aId));

        FacebookManager.getInstance().registerFriendListListener(FriendsManager.getInstance());
        FacebookManager.getInstance().onAppStart(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        FacebookManager.getInstance().onAppKilled();
    }
}
