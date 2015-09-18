package com.attilapalfi.exceptional.dependency_injection;

import javax.inject.Singleton;

import android.content.Context;
import com.attilapalfi.exceptional.ExceptionalApplication;
import com.attilapalfi.exceptional.services.ExceptionFactory;
import com.attilapalfi.exceptional.services.GpsService;
import com.attilapalfi.exceptional.services.facebook.FacebookManager;
import com.attilapalfi.exceptional.services.persistent_stores.*;
import com.attilapalfi.exceptional.services.rest.AppStartService;
import com.attilapalfi.exceptional.services.rest.ExceptionService;
import com.attilapalfi.exceptional.services.rest.RestInterfaceFactory;
import com.attilapalfi.exceptional.services.rest.VotingService;
import dagger.Module;
import dagger.Provides;

/**
 * Created by palfi on 2015-09-12.
 * <p>
 * This class provides the dependencies.
 */
@Module
public class AppContextModule {
    private final ExceptionalApplication application;

    public AppContextModule( ExceptionalApplication application ) {
        this.application = application;
    }

    @Provides
    @Singleton
    public ExceptionalApplication application( ) {
        return application;
    }

    @Provides
    @Singleton
    public Context applicationContext( ) {
        return application.getApplicationContext();
    }

    @Provides
    @Singleton
    public GpsService gpsService( ) {
        return new GpsService();
    }

    @Provides
    @Singleton
    public ExceptionFactory exceptionFactory( ) {
        return new ExceptionFactory();
    }

    @Provides
    @Singleton
    public ExceptionService backendService( ) {
        return new ExceptionService();
    }

    @Provides
    @Singleton
    public FacebookManager facebookManager( ) {
        return new FacebookManager();
    }

    @Provides
    @Singleton
    public AppStartService appStartService( ) {
        return new AppStartService();
    }

    @Provides
    @Singleton
    public ExceptionInstanceManager exceptionInstanceManager( ) {
        return new ExceptionInstanceManager();
    }

    @Provides
    @Singleton
    public ExceptionTypeManager exceptionTypeManager( ) {
        return new ExceptionTypeManager();
    }

    @Provides
    @Singleton
    public FriendStore friendsManager( ) {
        return new FriendStore();
    }

    @Provides
    @Singleton
    public ImageCache imageCache( ) {
        return new ImageCache();
    }

    @Provides
    @Singleton
    public MetadataStore metadataStore( ) {
        return new MetadataStore();
    }

    @Provides
    @Singleton
    public VotingService votingService( ) {
        return new VotingService();
    }

    @Provides
    @Singleton
    public RestInterfaceFactory restAdapterFactory( ) {
        return new RestInterfaceFactory();
    }
}
