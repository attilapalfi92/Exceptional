package com.attilapalf.exceptional;

import android.app.Application;

import com.attilapalf.exceptional.utils.LoginManager;

/**
 * Created by Attila on 2015-06-05.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LoginManager.onAppStart(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LoginManager.onAppKilled();
    }
}
