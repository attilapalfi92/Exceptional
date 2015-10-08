package com.attilapalfi.exceptional.dependency_injection;

import javax.inject.Singleton;

import android.content.Context;
import com.attilapalfi.exceptional.ExceptionalApplication;
import com.attilapalfi.exceptional.facebook.FacebookManager;
import com.attilapalfi.exceptional.model.ExceptionFactory;
import com.attilapalfi.exceptional.persistence.*;
import com.attilapalfi.exceptional.rest.AppStartRestConnector;
import com.attilapalfi.exceptional.rest.ExceptionRestConnector;
import com.attilapalfi.exceptional.rest.RestInterfaceFactory;
import com.attilapalfi.exceptional.rest.VotingRestConnector;
import com.attilapalfi.exceptional.services.LocationProvider;
import com.attilapalfi.exceptional.ui.helpers.QuestionNavigator;
import com.attilapalfi.exceptional.ui.helpers.ViewHelper;
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
    public ExceptionRestConnector backendService( ) {
        return new ExceptionRestConnector();
    }

    @Provides
    @Singleton
    public FacebookManager facebookManager( ) {
        return new FacebookManager();
    }

    @Provides
    @Singleton
    public AppStartRestConnector appStartService( ) {
        return new AppStartRestConnector();
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
    public VotingRestConnector votingService( ) {
        return new VotingRestConnector();
    }

    @Provides
    @Singleton
    public RestInterfaceFactory restAdapterFactory( ) {
        return new RestInterfaceFactory();
    }

    @Provides
    @Singleton
    public QuestionStore questionStore( ) {
        return new QuestionStore();
    }

    @Provides
    @Singleton
    public QuestionNavigator questionNavigator( ) {
        return new QuestionNavigator();
    }

    @Provides
    @Singleton
    public ViewHelper viewHelper( ) {
        return new ViewHelper();
    }
}
