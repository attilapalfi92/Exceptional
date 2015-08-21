package com.attilapalf.exceptional.services.facebook;

import android.app.Application;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.attilapalf.exceptional.MyApplication;
import com.attilapalf.exceptional.model.Friend;
import com.attilapalf.exceptional.services.ExceptionInstanceManager;
import com.attilapalf.exceptional.services.FriendsManager;
import com.attilapalf.exceptional.services.ImageCache;
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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
    private BigInteger profileId = new BigInteger("0");
    private Friend yourself;

    private CallbackManager callbackManager;
    private FacebookCallback<LoginResult> facebookCallback;
    private FacebookEventListener startupListener;
    private FacebookLoginSuccessHandler loginSuccessHandler;

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

    public void onAppStart(Application application) {
        initSubComponents(application);
        tokenTracker.startTracking();
        profileTracker.startTracking();
        accessToken = AccessToken.getCurrentAccessToken();
        refreshFriends();
    }

    private void initSubComponents(Application application) {
        FacebookSdk.sdkInitialize(application.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        initFacebookCallback();
        initTokenTracker();
        initProfileTracker();
    }

    private void initProfileTracker() {
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                synchronized (syncObject) {
                    if (newProfile != null) {
                        profile = newProfile;
                    }
                }
            }
        };
    }

    private void initTokenTracker() {
        tokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {
                synchronized (syncObject) {
                    if (newToken != null) {
                        setUserLoggedIn(newToken);
                    } else {
                        setUserLoggedOut();
                    }
                }
            }
        };
    }

    private void setUserLoggedOut() {
        FriendsManager.getInstance().wipe();
        ImageCache.getInstance().wipe();
        ExceptionInstanceManager.getInstance().wipe();
        MyApplication.setLoggedIn(false);
    }

    private void setUserLoggedIn(AccessToken newToken) {
        accessToken = newToken;
        if (!firstStart) {
            refreshFriends();
        }
        MyApplication.setLoggedIn(true);
    }

    private void initFacebookCallback() {
        facebookCallback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                synchronized (syncObject) {
                    accessToken = loginResult.getAccessToken();
                    loginSuccessHandler.onLoginSuccess(loginResult);
                    profile = Profile.getCurrentProfile();
                    firstStart = true;
                    MyApplication.setLoggedIn(true);
                    refreshFriends();
                }
            }
            @Override
            public void onCancel() {}
            @Override
            public void onError(FacebookException e) {
                MyApplication.setLoggedIn(false);
            }
        };
    }


    private void refreshFriends() {
        GraphRequest request = GraphRequest.newMyFriendsRequest(accessToken, new GraphRequest.GraphJSONArrayCallback() {
            @Override
            public void onCompleted(JSONArray jsonArray, GraphResponse graphResponse) {
                if (graphResponse.getError() == null) {
                    List<Friend> friends = parseFriendsJson(jsonArray);
                    updateProfileInBackground();
                    continueAppStart(friends);

                } else {
                    if (MyApplication.isLoggedIn()) {
                        startupListener.onNoInternetStart();
                    }
                }
            }
        });
        setLoginState();
        initGraphRequest(request);
    }

    private void setLoginState() {
        if (accessToken != null) {
            MyApplication.setLoggedIn(true);
        } else {
            MyApplication.setLoggedIn(false);
        }
    }

    private void initGraphRequest(GraphRequest request) {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void continueAppStart(List<Friend> friends) {
        if (MyApplication.isLoggedIn()) {
            if (firstStart) {
                startupListener.onFirstAppStart(friends, yourself);
            } else {
                startupListener.onRegularAppStart(friends, yourself);
            }
        }
    }

    @NonNull
    private List<Friend> parseFriendsJson(JSONArray jsonArray) {
        Log.d("response length: ", Integer.toString(jsonArray.length()));
        List<Friend> friends = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++) {
            try {
                Friend friend = parseFriend(jsonArray, i);
                friends.add(friend);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return friends;
    }

    private void updateProfileInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                profile = Profile.getCurrentProfile();
                if (profile != null) {
                    yourself = new Friend(
                            new BigInteger(profile.getId()),
                            profile.getFirstName() + " " + profile.getMiddleName(),
                            profile.getLastName(),
                            profile.getProfilePictureUri(200, 200).toString());
                    FriendsManager.getInstance().saveOrUpdateYourself(yourself);
                }
                return null;
            }
        }.execute();
    }

    @NonNull
    private Friend parseFriend(JSONArray jsonArray, int i) throws JSONException {
        JSONObject user = jsonArray.getJSONObject(i);
        String names[] = parseFirstAndLastName(user.getString("name"));
        String id = user.getString("id");
        JSONObject imageData = user.getJSONObject("picture").getJSONObject("data");
        String imageUrl = imageData.getString("url");
        return new Friend(new BigInteger(id), names[0], names[1], imageUrl, null);
    }

    private String[] parseFirstAndLastName(String name) {
        String[] names = name.split(" ");
        String[] firstAndLastName = {"", ""};
        firstAndLastName[1] = names[names.length - 1];
        for (int i = 0; i < names.length - 1; i++) {
            firstAndLastName[0] += names[i] + " ";
        }
        firstAndLastName[0] = firstAndLastName[0].trim();
        return firstAndLastName;
    }


    public BigInteger getProfileId() {
        if (profile == null) {
            profile = Profile.getCurrentProfile();
        }

        if (profileId.equals(new BigInteger("0"))) {
            profileId = new BigInteger(profile.getId());
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

    public void registerLoginSuccessHandler(FacebookLoginSuccessHandler handler) {
        loginSuccessHandler = handler;
    }

    public void registerFriendListListener(FacebookEventListener listener) {
        startupListener = listener;
    }
}
