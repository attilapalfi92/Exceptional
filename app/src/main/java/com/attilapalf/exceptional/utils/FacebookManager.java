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
    private static AccessToken accessToken;
    private static final Object syncObject = new Object();
    private static AccessTokenTracker tokenTracker;
    private static Profile profile;
    private static ProfileTracker profileTracker;

    private static CallbackManager callbackManager;
    private static FacebookCallback<LoginResult> facebookCallback;

    private static boolean friendRefreshFailed = true;

    // -------------------------------------------------------------------------------------
    // ------------------------------------- interfaces ------------------------------------
    // -------------------------------------------------------------------------------------
    public interface FriendListListener {
        void onFirstAppStart(Set<Friend> friendSet);
        void onAppStart(Set<Friend> friendSet);
    }

    public static FriendListListener friendListListener;

    public static void registerFriendListListener(FriendListListener listener) {
        friendListListener = listener;
    }

    // -------------------------------------------------------------------------------------

    public interface LoginSuccessHandler {
        void onLoginSuccess(LoginResult loginResult);
    }

    private static LoginSuccessHandler loginSuccessHandler;

    public static void registerLoginSuccessHandler(LoginSuccessHandler handler) {
        loginSuccessHandler = handler;
    }
    // -------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------



    public static void onAppStart(Application application) {
        FacebookSdk.sdkInitialize(application.getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        facebookCallback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                synchronized (syncObject) {
                    accessToken = loginResult.getAccessToken();
                    loginSuccessHandler.onLoginSuccess(loginResult);
                    profile = Profile.getCurrentProfile();
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

        synchronized (syncObject) {
            if (accessToken != null) {
                refreshFriends();
            }
        }
    }




    private static void refreshFriends() {
        GraphRequest request = GraphRequest.newMyFriendsRequest(accessToken, new GraphRequest.GraphJSONArrayCallback() {
            @Override
            public void onCompleted(JSONArray jsonArray, GraphResponse graphResponse) {
                // request successfully returned
                if (graphResponse.getError() == null) {
                    friendRefreshFailed = false;
                    Log.d("response length: ", Integer.toString(jsonArray.length()));
                    Set<Friend> friends = new TreeSet<>(new Friend.NameComparator());
                    for(int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject user = jsonArray.getJSONObject(i);
                            String name = user.getString("name");
                            String id = user.getString("id");
                            String imageUrl = user.getString("picture");
                            Friend friend = new Friend(Long.parseLong(id), name, imageUrl);
                            friends.add(friend);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    friendListListener.onFirstAppStart(friends);

                } else {
                    friendRefreshFailed = true;
                }
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }



    public static String getProfilId() {
        if (profile == null) {
            profile = Profile.getCurrentProfile();
        }

        return profile.getId();
    }


    public static boolean isUserLoggedIn() {
        synchronized (syncObject) {
            if (accessToken == null) {
                accessToken = AccessToken.getCurrentAccessToken();
            }
        }
        return accessToken != null;
    }


    public static void onAppKilled() {
        tokenTracker.stopTracking();
        profileTracker.stopTracking();
    }


    public static CallbackManager getCallbackManager() {
        return callbackManager;
    }


    public static FacebookCallback<LoginResult> getFacebookCallback() {
        return facebookCallback;
    }

    public static boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
