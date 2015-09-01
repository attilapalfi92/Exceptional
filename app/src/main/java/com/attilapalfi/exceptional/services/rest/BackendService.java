package com.attilapalfi.exceptional.services.rest;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.interfaces.ServerResponseListener;
import com.attilapalfi.exceptional.interfaces.ServerResponseSource;
import com.attilapalfi.exceptional.model.*;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.services.rest.messages.AppStartRequestBody;
import com.attilapalfi.exceptional.services.rest.messages.AppStartResponseBody;
import com.attilapalfi.exceptional.services.rest.messages.BaseExceptionRequestBody;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionRefreshResponse;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionSentResponse;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionInstanceWrapper;
import com.attilapalfi.exceptional.interfaces.ExceptionRefreshListener;
import com.attilapalfi.exceptional.interfaces.FriendChangeListener;
import com.attilapalfi.exceptional.interfaces.FriendSource;
import com.attilapalfi.exceptional.services.Converter;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionInstanceManager;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionTypeManager;
import com.attilapalfi.exceptional.services.persistent_stores.FriendsManager;
import com.attilapalfi.exceptional.services.persistent_stores.MetadataStore;
import com.attilapalfi.exceptional.services.facebook.FacebookManager;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

/**
 * Created by Attila on 2015-06-13.
 */
public class BackendService implements FriendSource, ServerResponseSource {

    private static BackendService instance;
    private final Context context;
    private final String projectNumber;

    private GoogleCloudMessaging googleCloudMessaging;
    private String registrationId;
    private String androidId;
    private RestInterface restInterface;
    private Set<ServerResponseListener> responseListeners = new HashSet<>();
    private Set<FriendChangeListener> friendChangeListeners = new HashSet<>();
    private AppStartRequestBody requestBody = new AppStartRequestBody();;

    public static void init(Context context) {
        instance = new BackendService(context);
    }

    public static BackendService getInstance() {
        return instance;
    }

    public BackendService setAndroidId(String aId) {
        androidId = aId;
        return this;
    }

    private BackendService(Context context){
        this.context = context;
        projectNumber = context.getString(R.string.project_number);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(context.getString(R.string.backend_address))
                .setConverter(new GsonConverter((new GsonBuilder().create())))
                .build();
        restInterface = restAdapter.create(RestInterface.class);
    }

    public void onFirstAppStart(List<Friend> friendList) {
        initRequestBody(friendList);
        requestBody.setDeviceName(getDeviceName());
        gcmFirstAppStart();
    }

    public void onRegularAppStart(List<Friend> friendList) {
        initRequestBody(friendList);
        requestBody.setExceptionVersion(MetadataStore.getInstance().getExceptionVersion());
        try {
            restInterface.regularAppStart(requestBody, new Callback<AppStartResponseBody>() {
                @Override
                public void success(AppStartResponseBody responseBody, Response response) {
                    saveExceptionInstancesToStore(responseBody);
                    saveExceptionTypesToStore(responseBody);
                    savePointsToStore(responseBody);
                    FriendsManager.getInstance().updateFriendsPoints(responseBody.getFriendsPoints());
                }

                @Override
                public void failure(RetrofitError error) {
                    for (ServerResponseListener l : responseListeners) {
                        l.onConnectionFailed("Connection to server failed.\n", error.getMessage());
                    }
                }
            });

        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

    public void throwException(Exception exception) {
        ExceptionInstanceWrapper exceptionInstanceWrapper = new ExceptionInstanceWrapper(exception);
        try{
            restInterface.sendException(exceptionInstanceWrapper, new Callback<ExceptionSentResponse>() {
                @Override
                public void success(ExceptionSentResponse e, Response response) {
                    Friend toWho = FriendsManager.getInstance().findFriendById(e.getInstanceWrapper().getToWho());
                    MetadataStore.getInstance().setPoints(e.getYourPoints());
                    FriendsManager.getInstance().updateFriendPoints(e.getInstanceWrapper().getToWho(), e.getFriendsPoints());
                    ExceptionInstanceManager.getInstance().addException(new Exception(e.getInstanceWrapper()), true);
                    for (ServerResponseListener l : responseListeners) {
                        l.onSuccess(e.getExceptionShortName() + " successfully sent to " + toWho.getName());
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    for (ServerResponseListener l : responseListeners) {
                        l.onConnectionFailed("Failed to throw the exception to the server.", error.getMessage());
                    }
                }
            });

        } catch (java.lang.Exception e) {
            for (ServerResponseListener l : responseListeners) {
                l.onConnectionFailed("Failed to throw the exception to the server.", e.getMessage());
            }
        }
    }

    public void refreshExceptions(final ExceptionRefreshListener refreshListener) {
        BaseExceptionRequestBody requestBody = new BaseExceptionRequestBody(
                FacebookManager.getInstance().getProfileId(),
                ExceptionInstanceManager.getInstance().getExceptionList()
        );

        restInterface.refreshExceptions(requestBody, new Callback<ExceptionRefreshResponse>() {
            @Override
            public void success(ExceptionRefreshResponse exceptionRefreshResponse, Response response) {
                for (ServerResponseListener l : responseListeners) {
                    l.onSuccess("Exceptions are synchronized!");
                }
                //ExceptionInstanceManager.getInstance().saveExceptions(exceptionRefreshResponse.getNeededExceptions());
                //for (ServerResponseListener l : responseListeners) {
                //    l.onSuccess("Exceptions are synchronized!");
                //}
                refreshListener.onExceptionRefreshFinished();
            }

            @Override
            public void failure(RetrofitError error) {
                for (ServerResponseListener l : responseListeners) {
                    l.onConnectionFailed("Failed to synchronize exceptions.\n", error.getMessage());
                }
                refreshListener.onExceptionRefreshFinished();
            }
        });
    }

    private void saveExceptionInstancesToStore(AppStartResponseBody responseBody) {
        ExceptionTypeManager.getInstance().setVotedExceptionTypes(responseBody.getBeingVotedTypes());
        if (responseBody.getMyExceptions().size() > 0) {
            ExceptionInstanceManager.getInstance().saveExceptions(responseBody.getMyExceptions());
        }
    }

    private void saveExceptionTypesToStore(AppStartResponseBody responseBody) {
        if (responseBody.getExceptionVersion() > MetadataStore.getInstance().getExceptionVersion()) {
            MetadataStore.getInstance().setExceptionVersion(responseBody.getExceptionVersion());
            ExceptionTypeManager.getInstance().addExceptionTypes(responseBody.getExceptionTypes());
        }
    }

    private void savePointsToStore(AppStartResponseBody responseBody) {
        if (responseBody.getPoints() != MetadataStore.getInstance().getPoints()) {
            MetadataStore.getInstance().setPoints(responseBody.getPoints());
        }
    }

    private void initRequestBody(List<Friend> friendList) {
        requestBody.setDeviceId(androidId);
        requestBody.setUserFacebookId(FacebookManager.getInstance().getProfileId());
        requestBody.setFriendsFacebookIds(Converter.fromFriendsToLongs(friendList));
        requestBody.setKnownExceptionIds(ExceptionInstanceManager.getInstance().getKnownIds());
        requestBody.setFirstName(FriendsManager.getInstance().getYourself().getFirstName());
        requestBody.setLastName(FriendsManager.getInstance().getYourself().getLastName());
    }


    private void gcmFirstAppStart() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String message;
                googleCloudMessaging = GoogleCloudMessaging.getInstance(context);
                try {
                    registrationId = googleCloudMessaging.register(projectNumber);
                    message = "Device registered, ID: " + registrationId;
                } catch (IOException e) {
                    message = "Error: " + e.getMessage();
                }
                return message;
            }

            @Override
            protected void onPostExecute(String message) {
                if (registrationId != null) {
                    requestBody.setGcmId(registrationId);
                    backendFirstAppStart();
                }
                for(ServerResponseListener l : responseListeners) {
                    l.onSuccess(message);
                }
            }

        }.execute();
    }

    private void backendFirstAppStart() {
        try {
            restInterface.firstAppStart(requestBody, new Callback<AppStartResponseBody>() {
                @Override
                public void success(AppStartResponseBody responseBody, Response response) {
                    saveDataToStores(responseBody);
                    if (responseBody.getMyExceptions().size() > 0) {
                        ExceptionInstanceManager.getInstance().saveExceptions(responseBody.getMyExceptions());
                    }
                    MetadataStore.getInstance().setFirstStartFinished(true);
                }

                @Override
                public void failure(RetrofitError error) {
                    for (ServerResponseListener l : responseListeners) {
                        l.onConnectionFailed("Connection to server failed.\n", error.getMessage());
                    }
                }
            });
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

    private void saveDataToStores(AppStartResponseBody responseBody) {
        ExceptionTypeManager.getInstance().addExceptionTypes(responseBody.getExceptionTypes());
        ExceptionTypeManager.getInstance().setVotedExceptionTypes(responseBody.getExceptionTypes());
        MetadataStore.getInstance().setExceptionVersion(responseBody.getExceptionVersion());
        MetadataStore.getInstance().setPoints(responseBody.getPoints());
        FriendsManager.getInstance().updateFriendsPoints(responseBody.getFriendsPoints());
    }

    @Override
    public boolean addResponseListener(ServerResponseListener listener) {
        return responseListeners.add(listener);
    }

    @Override
    public boolean removeResponseListener(ServerResponseListener listener) {
        return responseListeners.remove(listener);
    }

    @Override
    public boolean addFriendChangeListener(FriendChangeListener listener) {
        return friendChangeListeners.add(listener);
    }

    @Override
    public boolean removeFriendChangeListener(FriendChangeListener listener) {
        return friendChangeListeners.remove(listener);
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
