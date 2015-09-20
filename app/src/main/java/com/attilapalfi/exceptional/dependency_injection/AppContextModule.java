package com.attilapalfi.exceptional.dependency_injection;

import javax.inject.Singleton;

import android.content.Context;
import com.attilapalfi.exceptional.ExceptionalApplication;
import com.attilapalfi.exceptional.model.ExceptionFactory;
import com.attilapalfi.exceptional.persistence.*;
import com.attilapalfi.exceptional.services.LocationProvider;
import com.attilapalfi.exceptional.facebook.FacebookManager;
import com.attilapalfi.exceptional.rest.AppStartService;
import com.attilapalfi.exceptional.rest.ExceptionService;
import com.attilapalfi.exceptional.rest.RestInterfaceFactory;
import com.attilapalfi.exceptional.rest.VotingService;
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
    public LocationProvider gpsService( ) {
        return new LocationProvider();
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
    public ExceptionInstanceStore exceptionInstanceManager( ) {
        return new ExceptionInstanceStore();
    }

    @Provides
    @Singleton
    public ExceptionTypeStore exceptionTypeManager( ) {
        return new ExceptionTypeStore();
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
