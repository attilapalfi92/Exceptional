package com.attilapalfi.exceptional.dependency_injection;

import android.content.Context;
import com.attilapalfi.exceptional.ExceptionalApplication;
import com.attilapalfi.exceptional.model.ExceptionFactory;
import com.attilapalfi.exceptional.persistence.*;
import com.attilapalfi.exceptional.services.GpsService;
import com.attilapalfi.exceptional.facebook.FacebookManager;
import com.attilapalfi.exceptional.rest.AppStartService;
import com.attilapalfi.exceptional.rest.ExceptionService;
import com.attilapalfi.exceptional.rest.RestInterfaceFactory;
import com.attilapalfi.exceptional.rest.VotingService;

/**
 * Created by palfi on 2015-09-12.
 * <p>
 * provides the interface to get the object of classes that are injectable
 */
public interface AppContextComponent {
    ExceptionalApplication application( );

    Context applicationContext( );

    GpsService gpsService( );

    ExceptionFactory exceptionFactory( );

    ExceptionService backendService( );

    FacebookManager facebookManager( );

    AppStartService appStartService( );

    ExceptionInstanceStore exceptionInstanceManager( );

    ExceptionTypeStore exceptionTypeManager( );

    FriendStore friendsManager( );

    ImageCache imageCache( );

    MetadataStore metadataStore( );

    VotingService votingService( );

    RestInterfaceFactory restAdapterFactory( );
}
