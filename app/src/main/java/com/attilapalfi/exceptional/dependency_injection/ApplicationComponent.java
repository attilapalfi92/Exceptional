package com.attilapalfi.exceptional.dependency_injection;

import javax.inject.Singleton;

import com.attilapalfi.exceptional.ExceptionalApplication;
import com.attilapalfi.exceptional.model.ExceptionFactory;
import com.attilapalfi.exceptional.persistence.*;
import com.attilapalfi.exceptional.services.GcmMessageHandler;
import com.attilapalfi.exceptional.services.GpsService;
import com.attilapalfi.exceptional.facebook.FacebookManager;
import com.attilapalfi.exceptional.rest.AppStartService;
import com.attilapalfi.exceptional.rest.ExceptionService;
import com.attilapalfi.exceptional.rest.VotingService;
import com.attilapalfi.exceptional.ui.FacebookLoginFragment;
import com.attilapalfi.exceptional.ui.OptionsActivity;
import com.attilapalfi.exceptional.ui.ShowNotificationActivity;
import com.attilapalfi.exceptional.ui.main.ExceptionInstancesFragment;
import com.attilapalfi.exceptional.ui.main.MainActivity;
import com.attilapalfi.exceptional.ui.main.MainFragment;
import com.attilapalfi.exceptional.ui.main.VotedExceptionsFragment;
import com.attilapalfi.exceptional.ui.main.VotedExceptionsFragment.VotedExceptionAdapter;
import com.attilapalfi.exceptional.ui.main.friends_page.FriendDetailsActivity;
import com.attilapalfi.exceptional.ui.main.friends_page.FriendsFragment;
import com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing.ExceptionTypeChooserActivity;
import com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing.ExceptionTypePagerAdapter;
import com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing.ExceptionTypesFragment;
import com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing.ExceptionTypesFragment.ExceptionTypeAdapter;
import dagger.Component;

/**
 * Created by palfi on 2015-09-12.
 * <p>
 * an Application-scoped component that specifies where can be dependencies injected
 */
@Singleton
@Component( modules = AppContextModule.class )
public interface ApplicationComponent extends AppContextComponent {
    void inject( GpsService gpsService );
    void inject( ExceptionTypeAdapter exceptionTypeAdapter );
    void inject( ExceptionService exceptionService );
    void inject( ExceptionalApplication exceptionalApplication );
    void inject( FacebookManager facebookManager );
    void inject( FacebookLoginFragment facebookLoginFragment );
    void inject( MainActivity mainActivity );
    void inject( VotedExceptionsFragment votedExceptionsFragment );
    void inject( VotedExceptionAdapter votedExceptionAdapter );
    void inject( AppStartService appStartService );
    void inject( ExceptionInstanceManager exceptionInstanceManager );
    void inject( GcmMessageHandler gcmMessageHandler );
    void inject( ExceptionInstancesFragment exceptionInstancesFragment );
    void inject( ExceptionTypeManager exceptionTypeManager );
    void inject( ExceptionFactory exceptionFactory );
    void inject( ExceptionTypesFragment exceptionTypesFragment );
    void inject( ShowNotificationActivity showNotificationActivity );
    void inject( ExceptionTypeChooserActivity exceptionTypeChooserActivity );
    void inject( ExceptionTypePagerAdapter exceptionTypePagerAdapter );
    void inject( FriendStore friendStore );
    void inject( ImageCache imageCache );
    void inject( MainFragment mainFragment );
    void inject( FriendDetailsActivity friendDetailsActivity );
    void inject( FriendsFragment friendsFragment );
    void inject( MetadataStore metadataStore );
    void inject( OptionsActivity optionsActivity );
    void inject( VotingService votingService );
    void inject( ExceptionInstancesFragment.ExceptionInstanceAdapter exceptionInstanceAdapter );
}
