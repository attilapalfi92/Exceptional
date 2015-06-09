package com.attilapalf.exceptional.utils;

import android.app.Application;
import android.content.Intent;

import com.attilapalf.exceptional.exception.ExceptionFactory;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;

/**
 * Created by Attila on 2015-06-06.
 */
public class LoginManager {
    private static AccessToken accessToken;
    private static AccessTokenTracker tokenTracker;
    private static Profile profile;
    private static ProfileTracker profileTracker;

    private static CallbackManager callbackManager;
    private static FacebookCallback<LoginResult> facebookCallback;

    public interface LoginSuccessHandler {
        void onLoginSuccess(LoginResult loginResult);
    }

    private static LoginSuccessHandler loginSuccessHandler;

    public static void registerLoginSuccessHandler(LoginSuccessHandler handler) {
        loginSuccessHandler = handler;
    }


    public static void onAppStart(Application application) {
        FacebookSdk.sdkInitialize(application.getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        facebookCallback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                accessToken = loginResult.getAccessToken();
                loginSuccessHandler.onLoginSuccess(loginResult);
                profile = Profile.getCurrentProfile();
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
                accessToken = newToken;
            }
        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                profile = newProfile;
            }
        };

        tokenTracker.startTracking();
        profileTracker.startTracking();
        ExceptionFactory.initialize(application.getApplicationContext());
    }


    public static String getProfilId() {
        if (profile == null) {
            profile = Profile.getCurrentProfile();
        }

        return profile.getId();
    }


    public static boolean isUserLoggedIn() {
        if (accessToken == null) {
            accessToken = AccessToken.getCurrentAccessToken();
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
