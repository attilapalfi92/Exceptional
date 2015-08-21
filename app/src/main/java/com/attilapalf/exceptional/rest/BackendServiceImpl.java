package com.attilapalf.exceptional.rest;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.interfaces.ServerResponseListener;
import com.attilapalf.exceptional.interfaces.ServerResponseSource;
import com.attilapalf.exceptional.model.*;
import com.attilapalf.exceptional.model.Exception;
import com.attilapalf.exceptional.rest.messages.AppStartRequestBody;
import com.attilapalf.exceptional.rest.messages.AppStartResponseBody;
import com.attilapalf.exceptional.rest.messages.BaseExceptionRequestBody;
import com.attilapalf.exceptional.rest.messages.ExceptionRefreshResponse;
import com.attilapalf.exceptional.rest.messages.ExceptionSentResponse;
import com.attilapalf.exceptional.rest.messages.ExceptionWrapper;
import com.attilapalf.exceptional.interfaces.ExceptionRefreshListener;
import com.attilapalf.exceptional.interfaces.FriendChangeListener;
import com.attilapalf.exceptional.interfaces.FriendSource;
import com.attilapalf.exceptional.services.Converter;
import com.attilapalf.exceptional.services.ExceptionInstanceManager;
import com.attilapalf.exceptional.services.ExceptionTypeManager;
import com.attilapalf.exceptional.services.MetadataStore;
import com.attilapalf.exceptional.services.facebook.FacebookManager;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
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
public class BackendServiceImpl implements BackendService, FriendSource, ServerResponseSource {

    private static BackendServiceImpl instance;
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
        instance = new BackendServiceImpl(context);
    }

    public static BackendServiceImpl getInstance() {
        return instance;
    }

    public BackendServiceImpl setAndroidId(String aId) {
        androidId = aId;
        return this;
    }

    private BackendServiceImpl(Context context){
        this.context = context;
        projectNumber = context.getString(R.string.project_number);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(context.getString(R.string.backend_address))
                .setConverter(new GsonConverter((new GsonBuilder().create())))
                .build();
        restInterface = restAdapter.create(RestInterface.class);
    }

    @Override
    public void refreshExceptions(final ExceptionRefreshListener refreshListener) {
        BaseExceptionRequestBody requestBody = new BaseExceptionRequestBody(
                FacebookManager.getInstance().getProfileId(),
                ExceptionInstanceManager.getInstance().getExceptionList()
        );

        restInterface.refreshExceptions(requestBody, new Callback<ExceptionRefreshResponse>() {
            @Override
            public void success(ExceptionRefreshResponse exceptionRefreshResponse, Response response) {
                ExceptionInstanceManager.getInstance().addExceptions(exceptionRefreshResponse.getNeededExceptions());
                for (ServerResponseListener l : responseListeners) {
                    l.onSuccess("Exceptions are synchronized!");
                }
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



    @Override
    public void onRegularAppStart(List<Friend> friendList) {
        initRequestBody(friendList);
        requestBody.setExceptionVersion(MetadataStore.getInstance().getExceptionVersion());

        try {
            restInterface.regularAppStart(requestBody, new Callback<AppStartResponseBody>() {
                @Override
                public void success(AppStartResponseBody responseBody, Response response) {
                    ExceptionTypeManager.getInstance().setVotedExceptionTypes(responseBody.getBeingVotedTypes());
                    if (responseBody.getMyExceptions().size() > 0) {
                        ExceptionInstanceManager.getInstance().addExceptions(responseBody.getMyExceptions());
                    }
                    if (responseBody.getExceptionVersion() > MetadataStore.getInstance().getExceptionVersion()) {
                        MetadataStore.getInstance().setExceptionVersion(responseBody.getExceptionVersion());
                        ExceptionTypeManager.getInstance().addExceptionTypes2(responseBody.getExceptionTypes());
                    }
                    if (responseBody.getPoints() != MetadataStore.getInstance().getPoints()) {
                        MetadataStore.getInstance().setPoints(responseBody.getPoints());
                    }
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

    @Override
    public void onFirstAppStart(List<Friend> friendList) {
        initRequestBody(friendList);
        requestBody.setDeviceName(getDeviceName());
        gcmFirstAppStart();
    }

    private void initRequestBody(List<Friend> friendList) {
        requestBody.setDeviceId(androidId);
        requestBody.setUserFacebookId(FacebookManager.getInstance().getProfileId());
        requestBody.setFriendsFacebookIds(Converter.fromFriendsToLongs(friendList));
        requestBody.setKnownExceptionIds(new ArrayList<BigInteger>());
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
                    ExceptionTypeManager.getInstance().addExceptionTypes(responseBody.getExceptionTypes());
                    ExceptionTypeManager.getInstance().setVotedExceptionTypes(responseBody.getExceptionTypes());
                    MetadataStore.getInstance().setExceptionVersion(responseBody.getExceptionVersion());
                    MetadataStore.getInstance().setPoints(responseBody.getPoints());
                    if (responseBody.getMyExceptions().size() > 0) {
                        ExceptionInstanceManager.getInstance().addExceptions(responseBody.getMyExceptions());
                    }
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

    @Override
    public void sendException(Exception e) {
        ExceptionWrapper exceptionWrapper = new ExceptionWrapper(e);

        try{
            restInterface.sendException(exceptionWrapper, new Callback<ExceptionSentResponse>() {
                @Override
                public void success(ExceptionSentResponse e, Response response) {
                    for (ServerResponseListener l : responseListeners) {
                        l.onSuccess(e.getShortName() + " successfully sent to " + e.getToWho());
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    for (ServerResponseListener l : responseListeners) {
                        l.onConnectionFailed("Failed to send the exception to the server.", error.getMessage());
                    }
                }
            });

        } catch (java.lang.Exception exception) {

        }
    }

    @Override
    public boolean addConnectionListener(ServerResponseListener listener) {
        return responseListeners.add(listener);
    }

    @Override
    public boolean removeConnectionListener(ServerResponseListener listener) {
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
