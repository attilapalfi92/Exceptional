package com.attilapalf.exceptional.utils;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.attilapalf.exceptional.model.Friend;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Attila on 2015-06-06.
 */
public class FacebookManager {
    private AccessToken accessToken;
    /**
     * Protects the accessToken and the profile objects
     */
    private final Object syncObject = new Object();
    private AccessTokenTracker tokenTracker;
    private Profile profile;
    private ProfileTracker profileTracker;
    private long profileId = 0;

    private CallbackManager callbackManager;
    private FacebookCallback<LoginResult> facebookCallback;

    private boolean firstStart = false;

    private static FacebookManager instance;

    public static FacebookManager getInstance() {
        if (instance == null) {
            instance = new FacebookManager();
        }

        return instance;
    }

    private FacebookManager() {
    }


//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }

    // -------------------------------------------------------------------------------------
    // ------------------------------------- interfaces ------------------------------------
    // -------------------------------------------------------------------------------------

    /**
     * When the user starts the application, first thing is that it sends a request to
     * Facebook to get his friends. When facebook sends his friends back, these methods are called.
     */
    public interface FriendListListener {
        void onFirstAppStart(Set<Friend> friendSet);
        void onAppStart(Set<Friend> friendSet);
    }

    public FriendListListener friendListListener;

    public void registerFriendListListener(FriendListListener listener) {
        friendListListener = listener;
    }

    // -------------------------------------------------------------------------------------

    /**
     * If the user successfully logs in, the FbLoginFragment start hte main activity
     */
    public interface LoginSuccessHandler {
        void onLoginSuccess(LoginResult loginResult);
    }

    private LoginSuccessHandler loginSuccessHandler;

    public void registerLoginSuccessHandler(LoginSuccessHandler handler) {
        loginSuccessHandler = handler;
    }
    // -------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------



    public void onAppStart(Application application) {
        FacebookSdk.sdkInitialize(application.getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        facebookCallback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                synchronized (syncObject) {
                    accessToken = loginResult.getAccessToken();
                    loginSuccessHandler.onLoginSuccess(loginResult);
                    profile = Profile.getCurrentProfile();
                    firstStart = true;
                    refreshFriends();
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }
        };


        tokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {
                synchronized (syncObject) {
                    accessToken = newToken;
                    if (!firstStart) {
                        refreshFriends();
                    }
                }
            }
        };


        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                synchronized (syncObject) {
                    profile = newProfile;
                }
            }
        };

        tokenTracker.startTracking();
        profileTracker.startTracking();
        accessToken = AccessToken.getCurrentAccessToken();

        // experimental:
        refreshFriends();
    }




    private void refreshFriends() {
        GraphRequest request = GraphRequest.newMyFriendsRequest(accessToken, new GraphRequest.GraphJSONArrayCallback() {

            // this method is called from the main thread, so... if it takes too long to process,
            // I'll have to put the processing in a background thread
            @Override
            public void onCompleted(JSONArray jsonArray, GraphResponse graphResponse) {
                // request successfully returned
                if (graphResponse.getError() == null) {
                    Log.d("response length: ", Integer.toString(jsonArray.length()));
                    Set<Friend> friends = new TreeSet<>(new Friend.NameComparator());
                    for(int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject user = jsonArray.getJSONObject(i);
                            String name = user.getString("name");
                            String id = user.getString("id");
                            JSONObject imageData = user.getJSONObject("picture").getJSONObject("data");
                            String imageUrl = imageData.getString("url");
                            Friend friend = new Friend(Long.parseLong(id), name, imageUrl, null);
                            friends.add(friend);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (firstStart) {
                        friendListListener.onFirstAppStart(friends);
                    } else {
                        friendListListener.onAppStart(friends);
                    }
                }
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }




    public void testAsyncCall() {

    }



    public long getProfileId() {
        if (profile == null) {
            profile = Profile.getCurrentProfile();
        }

        if (profileId == 0) {
            profileId = Long.parseLong(profile.getId());
        }

        return profileId;
    }


    public boolean isUserLoggedIn() {
        synchronized (syncObject) {
            if (accessToken == null) {
                accessToken = AccessToken.getCurrentAccessToken();
            }
        }
        return accessToken != null;
    }


    public void onAppKilled() {
        tokenTracker.stopTracking();
        profileTracker.stopTracking();
    }


    public CallbackManager getCallbackManager() {
        return callbackManager;
    }


    public FacebookCallback<LoginResult> getFacebookCallback() {
        return facebookCallback;
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
