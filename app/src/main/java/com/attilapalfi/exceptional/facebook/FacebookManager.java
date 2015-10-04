package com.attilapalfi.exceptional.facebook;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.model.Friend;
import com.attilapalfi.exceptional.persistence.*;
import com.attilapalfi.exceptional.rest.AppStartService;
import com.facebook.*;
import com.facebook.login.LoginResult;

/**
 * Created by Attila on 2015-06-06.
 */
public class FacebookManager {
    @Inject
    AppStartService appStartService;
    @Inject
    ExceptionInstanceStore exceptionInstanceStore;
    @Inject
    ExceptionTypeStore exceptionTypeStore;
    @Inject
    FriendStore friendStore;
    @Inject
    ImageCache imageCache;
    @Inject
    MetadataStore metadataStore;
    private AccessToken accessToken;
    private AccessTokenTracker tokenTracker;
    private Profile profile;
    private ProfileTracker profileTracker;
    @NonNull
    private final Friend user = new Friend();
    private CallbackManager callbackManager;
    private FacebookCallback<LoginResult> facebookCallback;
    private FacebookLoginSuccessHandler loginSuccessHandler;

    public FacebookManager( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
    }

    public void onAppStart( Application application ) {
        initSubComponents( application );
        tokenTracker.startTracking();
        profileTracker.startTracking();
        accessToken = AccessToken.getCurrentAccessToken();
        if ( accessToken != null ) {
            refreshFriends();
        }
    }

    private void initSubComponents( Application application ) {
        FacebookSdk.sdkInitialize( application.getApplicationContext() );
        callbackManager = CallbackManager.Factory.create();
        initFacebookCallback();
        initTokenTracker();
        initProfileTracker();
    }

    private void initProfileTracker( ) {
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged( Profile oldProfile, Profile newProfile ) {
                if ( newProfile != null ) {
                    profile = newProfile;
                }
            }
        };
    }

    private void initTokenTracker( ) {
        tokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged( AccessToken oldToken, AccessToken newToken ) {
                accessToken = newToken;
                if ( newToken == null ) {
                    setUserLoggedOut();
                } else {
                    refreshFriends();
                }
            }
        };
    }

    private void setUserLoggedOut( ) {
        imageCache.wipe( friendStore.getStoredFriends() );
        friendStore.wipe();
        exceptionInstanceStore.wipe();
        exceptionTypeStore.wipe();
        metadataStore.wipe();
    }

    private void initFacebookCallback( ) {
        facebookCallback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess( LoginResult loginResult ) {
                accessToken = loginResult.getAccessToken();
                loginSuccessHandler.onLoginSuccess( loginResult );
                initYourself();
                metadataStore.setLoggedIn( true );
            }

            @Override
            public void onCancel( ) {
            }

            @Override
            public void onError( FacebookException e ) {
            }
        };
    }

    private void refreshFriends( ) {
        GraphRequest request = GraphRequest.newMyFriendsRequest( accessToken, ( jsonArray, graphResponse ) -> {
            if ( graphResponse.getError() == null ) {
                //List<Friend> friends = parseFriendsJson( jsonArray );
                //continueAppStart( friends );
                new Thread( () -> {
                    List<Friend> friends = parseFriendsJson( jsonArray );
                    continueAppStart( friends );
                } ).start();
            } else {
                if ( metadataStore.isLoggedIn() ) {
                    Log.e( "FacebookManager: ", "GraphRequest error: " + graphResponse.getError() );
                }
            }
        } );
        executeGraphRequest( request );
    }

    private void executeGraphRequest( GraphRequest request ) {
        Bundle parameters = new Bundle();
        parameters.putString( "fields", "id,name,picture" );
        request.setParameters( parameters );
        request.executeAsync();
    }

    @NonNull
    private List<Friend> parseFriendsJson( JSONArray jsonArray ) {
        Log.d( "response length: ", Integer.toString( jsonArray.length() ) );
        List<Friend> friends = new ArrayList<>();
        for ( int i = 0; i < jsonArray.length(); i++ ) {
            try {
                Friend friend = parseFriend( jsonArray, i );
                friends.add( friend );
            } catch ( JSONException e ) {
                e.printStackTrace();
            }
        }
        return friends;
    }

    private void continueAppStart( List<Friend> friends ) {
        if ( metadataStore.isLoggedIn() ) {
            initYourself();
            metadataStore.updateUser( user );
            friendStore.updateFriendList( friends );
            if ( !metadataStore.isFirstStartFinished() ) {
                appStartService.onFirstAppStart( friends, user.getId() );
            } else {
                appStartService.onRegularAppStart( friends, user.getId() );
            }
        }
    }

    private void initYourself( ) {
        profile = Profile.getCurrentProfile();
        if ( profile != null ) {
            user.setId( new BigInteger( profile.getId() ) );
            user.setFirstName( ( profile.getFirstName().trim() + " " + profile.getMiddleName().trim() ).trim() );
            user.setLastName( profile.getLastName() );
            user.setImageUrl( profile.getProfilePictureUri( 200, 200 ).toString() );
            user.setPoints( 100 );
            user.setImageLoaded( false );
        }
    }

    @NonNull
    private Friend parseFriend( JSONArray jsonArray, int i ) throws JSONException {
        JSONObject user = jsonArray.getJSONObject( i );
        String names[] = parseFirstAndLastName( user.getString( "name" ) );
        String id = user.getString( "id" );
        JSONObject imageData = user.getJSONObject( "picture" ).getJSONObject( "data" );
        String imageUrl = imageData.getString( "url" );
        return new Friend( new BigInteger( id ), names[0], names[1], imageUrl, 100, false );
    }

    private String[] parseFirstAndLastName( String name ) {
        String[] names = name.split( " " );
        String[] firstAndLastName = { "", "" };
        firstAndLastName[1] = names[names.length - 1];
        for ( int i = 0; i < names.length - 1; i++ ) {
            firstAndLastName[0] += names[i] + " ";
        }
        firstAndLastName[0] = firstAndLastName[0].trim();
        return firstAndLastName;
    }

    public void onAppKilled( ) {
        tokenTracker.stopTracking();
        profileTracker.stopTracking();
    }

    public CallbackManager getCallbackManager( ) {
        return callbackManager;
    }

    public FacebookCallback<LoginResult> getFacebookCallback( ) {
        return facebookCallback;
    }

    public boolean onActivityResult( int requestCode, int resultCode, Intent data ) {
        return callbackManager.onActivityResult( requestCode, resultCode, data );
    }

    public void registerLoginSuccessHandler( FacebookLoginSuccessHandler handler ) {
        loginSuccessHandler = handler;
    }
}
