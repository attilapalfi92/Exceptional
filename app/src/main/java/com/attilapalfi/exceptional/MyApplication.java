package com.attilapalfi.exceptional;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.attilapalfi.exceptional.services.rest.BackendService;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionTypeManager;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionInstanceManager;
import com.attilapalfi.exceptional.services.persistent_stores.MetadataStore;
import com.attilapalfi.exceptional.services.facebook.FacebookManager;
import com.attilapalfi.exceptional.services.persistent_stores.FriendsManager;
import com.attilapalfi.exceptional.services.persistent_stores.ImageCache;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Attila on 2015-06-05.
 */
public class MyApplication extends android.support.multidex.MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        writeHashKeyToDebug();
        Context applicationContext = getApplicationContext();
        initializeServices(applicationContext);
        FacebookManager.onAppStart(this);
    }

    private void initializeServices(Context applicationContext) {
        MetadataStore.initialize(applicationContext);
        ImageCache.initialize(applicationContext);
        ExceptionTypeManager.initialize(applicationContext);
        ExceptionInstanceManager.initialize(applicationContext);
        FriendsManager.initialize(applicationContext);
        String androidId = Settings.Secure.getString(applicationContext
                .getContentResolver(), Settings.Secure.ANDROID_ID);
        BackendService.init(applicationContext);
        BackendService.setAndroidId(androidId);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        FacebookManager.onAppKilled();
    }


    private void writeHashKeyToDebug() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.attilapalfi.exceptional",
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
}
