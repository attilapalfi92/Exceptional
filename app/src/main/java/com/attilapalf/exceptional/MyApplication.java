package com.attilapalf.exceptional;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.attilapalf.exceptional.rest.BackendConnector;
import com.attilapalf.exceptional.services.ExceptionFactory;
import com.attilapalf.exceptional.services.ExceptionManager;
import com.attilapalf.exceptional.services.FacebookManager;
import com.attilapalf.exceptional.services.FriendsManager;
import com.attilapalf.exceptional.services.ImageCache;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Attila on 2015-06-05.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();


        // write release hash key as debug message
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
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


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
