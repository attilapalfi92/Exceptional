package com.attilapalfi.exceptional.services.facebook;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.attilapalfi.exceptional.model.Friend;
import com.attilapalfi.exceptional.services.rest.BackendService;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionInstanceManager;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionTypeManager;
import com.attilapalfi.exceptional.services.persistent_stores.FriendsManager;
import com.attilapalfi.exceptional.services.persistent_stores.ImageCache;
import com.attilapalfi.exceptional.services.persistent_stores.MetadataStore;
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
    private static AccessToken accessToken;
    private static AccessTokenTracker tokenTracker;
    private static Profile profile;
    private static ProfileTracker profileTracker;
    private static BigInteger profileId = new BigInteger("0");
    private static Friend yourself;
    private static CallbackManager callbackManager;
    private static FacebookCallback<LoginResult> facebookCallback;
    private static FacebookLoginSuccessHandler loginSuccessHandler;

    private FacebookManager() {
    }

    public static void onAppStart(Application application) {
        initSubComponents(application);
        tokenTracker.startTracking();
        profileTracker.startTracking();
        accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            refreshFriends();
        }
    }

    private static void initSubComponents(Application application) {
        FacebookSdk.sdkInitialize(application.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        initFacebookCallback();
        initTokenTracker();
        initProfileTracker();
    }

    private static void initProfileTracker() {
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                if (newProfile != null) {
                    profile = newProfile;
                }
            }
        };
    }

    private static void initTokenTracker() {
        tokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {
                accessToken = newToken;
                if (newToken == null) {
                    setUserLoggedOut();
                } else {
                    refreshFriends();
                }
            }
        };
    }

    private static void setUserLoggedOut() {
        ImageCache.wipe();
        FriendsManager.wipe();
        ExceptionInstanceManager.wipe();
        ExceptionTypeManager.wipe();
        MetadataStore.wipe();
    }

    private static void initFacebookCallback() {
        facebookCallback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                accessToken = loginResult.getAccessToken();
                loginSuccessHandler.onLoginSuccess(loginResult);
                initYourself();
                MetadataStore.setLoggedIn(true);
            }
            @Override
            public void onCancel() {}
            @Override
            public void onError(FacebookException e) {}
        };
    }


    private static void refreshFriends() {
        GraphRequest request = GraphRequest.newMyFriendsRequest(accessToken, (jsonArray, graphResponse) -> {
            if (graphResponse.getError() == null) {
                List<Friend> friends = parseFriendsJson(jsonArray);
                continueAppStart(friends);
            } else {
                if (MetadataStore.isLoggedIn()) {
                    Log.e("FacebookManager: ", "GraphRequest error: " + graphResponse.getError());
                }
            }
        });
        executeGraphRequest(request);
    }

    @NonNull
    private static List<Friend> parseFriendsJson(JSONArray jsonArray) {
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

    private static void continueAppStart(List<Friend> friends) {
        Log.i("FacebookManager: ", "continueAppStart called.");
        if (MetadataStore.isLoggedIn()) {
            if (!MetadataStore.isFirstStartFinished()) {
                initYourself();
                FriendsManager.saveFriendsAndYourself(friends, yourself);
                BackendService.onFirstAppStart(friends);
            } else {
                initYourself();
                FriendsManager.updateFriendsAndYourself(friends, yourself);
                BackendService.onRegularAppStart(friends);
            }
        }
    }

    private static void executeGraphRequest(GraphRequest request) {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private static void initYourself() {
        profile = Profile.getCurrentProfile();
        if (profile != null) {
            yourself = new Friend(
                    new BigInteger(profile.getId()),
                    profile.getFirstName() + " " + profile.getMiddleName(),
                    profile.getLastName(),
                    profile.getProfilePictureUri(200, 200).toString());
        }
    }

    @NonNull
    private static Friend parseFriend(JSONArray jsonArray, int i) throws JSONException {
        JSONObject user = jsonArray.getJSONObject(i);
        String names[] = parseFirstAndLastName(user.getString("name"));
        String id = user.getString("id");
        JSONObject imageData = user.getJSONObject("picture").getJSONObject("data");
        String imageUrl = imageData.getString("url");
        return new Friend(new BigInteger(id), names[0], names[1], imageUrl, null);
    }

    private static String[] parseFirstAndLastName(String name) {
        String[] names = name.split(" ");
        String[] firstAndLastName = {"", ""};
        firstAndLastName[1] = names[names.length - 1];
        for (int i = 0; i < names.length - 1; i++) {
            firstAndLastName[0] += names[i] + " ";
        }
        firstAndLastName[0] = firstAndLastName[0].trim();
        return firstAndLastName;
    }


    public static BigInteger getProfileId() {
        if (profile == null) {
            profile = Profile.getCurrentProfile();
        }

        if (profileId.equals(new BigInteger("0"))) {
            profileId = new BigInteger(profile.getId());
        }

        return profileId;
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

    public static void registerLoginSuccessHandler(FacebookLoginSuccessHandler handler) {
        loginSuccessHandler = handler;
    }
}
