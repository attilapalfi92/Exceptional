package com.attilapalfi.exceptional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.facebook.FacebookManager;
import com.attilapalfi.exceptional.persistence.StoreInitializer;
import com.attilapalfi.exceptional.rest.AppStartRestConnector;
import com.attilapalfi.exceptional.services.LocationProvider;
import io.paperdb.Paper;

/**
 * Created by Attila on 2015-06-05.
 */
public class ExceptionalApplication extends android.support.multidex.MultiDexApplication {
    @Inject
    AppStartRestConnector appStartRestConnector;
    @Inject
    StoreInitializer storeInitializer;
    @Inject
    FacebookManager facebookManager;
    @Inject
    LocationProvider locationProvider;

    @Override
    public void onCreate( ) {
        super.onCreate();
        Injector.INSTANCE.initializeApplicationComponent( this );
        Context applicationContext = getApplicationContext();
        initializeServices( applicationContext );
        storeInitializer.initialize( this );
    }

    private void initializeServices( Context applicationContext ) {
        Paper.init( applicationContext );
        Injector.INSTANCE.getApplicationComponent().inject( this );
        String androidId = Settings.Secure.getString( applicationContext
                .getContentResolver(), Settings.Secure.ANDROID_ID );
        appStartRestConnector.setAndroidId( androidId );
    }

    @Override
    public void onTerminate( ) {
        super.onTerminate();
        facebookManager.onAppKilled();
        locationProvider.stopLocationUpdates();
    }

    private void writeHashKeyToDebug( ) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.attilapalfi.exceptional",
                    PackageManager.GET_SIGNATURES );
            for ( Signature signature : info.signatures ) {
                MessageDigest md = MessageDigest.getInstance( "SHA" );
                md.update( signature.toByteArray() );
                String hash = Base64.encodeToString( md.digest(), Base64.DEFAULT );
                Log.d( "KeyHash:", hash );
            }
        } catch ( PackageManager.NameNotFoundException | NoSuchAlgorithmException e ) {
            e.printStackTrace();
        }
    }
}
