package com.attilapalfi.exceptional.dependency_injection;

import android.content.Context;
import com.attilapalfi.exceptional.ExceptionalApplication;
import com.attilapalfi.exceptional.facebook.FacebookManager;
import com.attilapalfi.exceptional.model.ExceptionFactory;
import com.attilapalfi.exceptional.model.ExceptionHelper;
import com.attilapalfi.exceptional.persistence.*;
import com.attilapalfi.exceptional.rest.*;
import com.attilapalfi.exceptional.services.LocationProvider;
import com.attilapalfi.exceptional.ui.helpers.QuestionNavigator;

/**
 * Created by palfi on 2015-09-12.
 * <p>
 * provides the interface to get the object of classes that are injectable
 */
public interface AppContextComponent {
    ExceptionalApplication application( );

    Context applicationContext( );

    LocationProvider gpsService( );

    ExceptionFactory exceptionFactory( );

    ExceptionRestConnector backendService( );

    FacebookManager facebookManager( );

    AppStartRestConnector appStartService( );

    ExceptionInstanceStore exceptionInstanceManager( );

    ExceptionTypeStore exceptionTypeManager( );

    FriendStore friendsManager( );

    ImageCache imageCache( );

    MetadataStore metadataStore( );

    VotingRestConnector votingService( );

    RestInterfaceFactory restAdapterFactory( );

    QuestionStore questionStore( );

    QuestionNavigator questionNavigator( );

    ExceptionHelper viewHelper( );

    StatSupplier statSupplier( );

    StoreInitializer storeInitializer( );
}
