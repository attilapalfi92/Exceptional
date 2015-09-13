package com.attilapalfi.exceptional.dependency_injection;

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

    ExceptionInstanceManager exceptionInstanceManager( );

    ExceptionTypeManager exceptionTypeManager( );

    FriendsManager friendsManager( );

    ImageCache imageCache( );

    MetadataStore metadataStore( );

    VotingService votingService( );

    RestInterfaceFactory restAdapterFactory( );
}
