package com.attilapalf.exceptional;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.attilapalf.exceptional.rest.BackendServiceImpl;
import com.attilapalf.exceptional.services.ExceptionTypeManager;
import com.attilapalf.exceptional.services.ExceptionInstanceManager;
import com.attilapalf.exceptional.services.MetadataStore;
import com.attilapalf.exceptional.services.facebook.FacebookManager;
import com.attilapalf.exceptional.services.FriendsManager;
import com.attilapalf.exceptional.services.ImageCache;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Attila on 2015-06-05.
 */
public class MyApplication extends Application {

    private static volatile boolean loggedIn = false;

    @Override
    public void onCreate() {
        super.onCreate();
        writeHashKeyToDebug();
        Context applicationContext = getApplicationContext();
        initializeServices(applicationContext);
        FacebookManager.getInstance().onAppStart(this);
    }

    private void initializeServices(Context applicationContext) {
        MetadataStore.getInstance().initialize(applicationContext);
        ImageCache.getInstance().initialize(applicationContext);
        ExceptionTypeManager.getInstance().initialize(applicationContext);
        ExceptionInstanceManager.getInstance().initialize(applicationContext);
        FriendsManager.getInstance().initialize(applicationContext);
        String androidId = Settings.Secure.getString(applicationContext
                .getContentResolver(), Settings.Secure.ANDROID_ID);
        BackendServiceImpl.init(applicationContext);
        FriendsManager.getInstance().addBackendService(BackendServiceImpl.getInstance().setAndroidId(androidId));
        FacebookManager.getInstance().registerFriendListListener(FriendsManager.getInstance());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        FacebookManager.getInstance().onAppKilled();
    }


    private void writeHashKeyToDebug() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.attilapalf.exceptional",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.d("KeyHash:", hash);
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    public static void setLoggedIn(boolean loggedIn) {
        MyApplication.loggedIn = loggedIn;
    }
}
