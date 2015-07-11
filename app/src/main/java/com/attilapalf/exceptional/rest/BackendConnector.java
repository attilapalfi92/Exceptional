package com.attilapalf.exceptional.rest;

import android.content.Context;
import android.os.AsyncTask;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.interfaces.BackendService;
import com.attilapalf.exceptional.interfaces.ServerResponseListener;
import com.attilapalf.exceptional.interfaces.ServerResponseSource;
import com.attilapalf.exceptional.model.*;
import com.attilapalf.exceptional.model.Exception;
import com.attilapalf.exceptional.rest.messages.AppStartRequestBody;
import com.attilapalf.exceptional.rest.messages.AppStartResponseBody;
import com.attilapalf.exceptional.rest.messages.BaseRequestBody;
import com.attilapalf.exceptional.rest.messages.ExceptionRefreshResponse;
import com.attilapalf.exceptional.rest.messages.ExceptionSentResponse;
import com.attilapalf.exceptional.rest.messages.ExceptionWrapper;
import com.attilapalf.exceptional.interfaces.ExceptionRefreshListener;
import com.attilapalf.exceptional.interfaces.FriendChangeListener;
import com.attilapalf.exceptional.interfaces.FriendSource;
import com.attilapalf.exceptional.services.ExceptionManager;
import com.attilapalf.exceptional.services.FacebookManager;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by Attila on 2015-06-13.
 */
public class BackendConnector implements BackendService, FriendSource, //ExceptionSource
        ServerResponseSource {

    private final Context context;
    private final String projectNumber;
    private GoogleCloudMessaging googleCloudMessaging;
    private String registrationId;

    private String androidId;
    private Gson gson;
    private RestAdapter restAdapter;
    private RestInterface restInterface;
    private Set<ServerResponseListener> responseListeners;
//    private Set<ExceptionChangeListener> exceptionChangeListeners;
    private Set<FriendChangeListener> friendChangeListeners;
    private ExceptionRefreshListener refreshListener;

    private AppStartRequestBody requestBody;

    // http://188.36.23.85:8090
    private final String backendAddress = "http://188.36.23.85:8090";

    private static BackendConnector instance;


    public static void init(Context context) {
        instance = new BackendConnector(context);
    }


    // TODO: separate to init and getInstance
    public static BackendConnector getInstance() {
        return instance;
    }

    public BackendConnector setAndroidId(String aId) {
        androidId = aId;
        return this;
    }

    private BackendConnector(Context context){
        this.context = context;

        projectNumber = context.getString(R.string.project_number);

        responseListeners = new HashSet<>();
        friendChangeListeners = new HashSet<>();

        gson = new GsonBuilder()
                .create();

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(backendAddress)
                .setConverter(new GsonConverter((gson)))
                .build();

        restInterface = restAdapter.create(RestInterface.class);
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


    public interface RestInterface {
        @POST("/user/firstAppStart")
        void firstAppStart(@Body AppStartRequestBody requestBody, Callback<AppStartResponseBody> cb);

        @POST("/user/appStart")
        void appStart(@Body AppStartRequestBody requestBody, Callback<AppStartResponseBody> cb);

        @POST("/exception")
        void sendException(@Body ExceptionWrapper exceptionWrapper, Callback<ExceptionSentResponse> cb);

        @POST("/exception/refresh")
        void refreshExceptions(@Body BaseRequestBody requestBody, Callback<ExceptionRefreshResponse> cb);
    }


    @Override
    public void refreshExceptions(final ExceptionRefreshListener refreshListenerParam) {
        this.refreshListener = refreshListenerParam;

        BaseRequestBody requestBody = new BaseRequestBody(FacebookManager.getInstance().getProfileId(),
                ExceptionManager.getInstance().getExceptionList());

        restInterface.refreshExceptions(requestBody, new Callback<ExceptionRefreshResponse>() {

            @Override
            public void success(ExceptionRefreshResponse exceptionRefreshResponse, Response response) {
                ExceptionManager.getInstance().addExceptions(exceptionRefreshResponse.getNeededExceptions());

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
    public void onAppStart() {
        Long lastKnownExcId = ExceptionManager.getInstance().getLastKnownId();
        List<Long> exceptionIds = new ArrayList<>();
        exceptionIds.add(lastKnownExcId);
        long userId = FacebookManager.getInstance().getProfileId();
        AppStartRequestBody requestBody = new AppStartRequestBody(androidId, userId,
                new ArrayList<Long>(), exceptionIds);

        try {
            restInterface.appStart(requestBody, new Callback<AppStartResponseBody>() {
                @Override
                public void success(AppStartResponseBody appStartResponseBody, Response response) {
                    int excSize = appStartResponseBody.getMyExceptions().size();
                    if (excSize > 0) {
                        ExceptionManager.getInstance().addExceptions(appStartResponseBody.getMyExceptions());
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
    public void onFirstAppStart(Set<Friend> friendSet) {
        long userId = FacebookManager.getInstance().getProfileId();
        List<Long> friendIdList = new ArrayList<>(friendSet.size());
        for(Friend f : friendSet) {
            friendIdList.add(f.getId());
        }

        requestBody = new AppStartRequestBody();
        requestBody.setDeviceId(androidId);
        requestBody.setUserId(userId);
        requestBody.setFriendsIds(friendIdList);
        requestBody.setExceptionIds(new ArrayList<Long>());

        gcmFirstAppStart();
    }


    // TODO: private
    public void gcmFirstAppStart() {
        new AsyncTask<Void, Void, String>() {

            String message;

            @Override
            protected String doInBackground(Void... params) {
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
            protected void onPostExecute(String s) {

                if (registrationId != null) {
                    requestBody.setRegId(registrationId);
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
                public void success(AppStartResponseBody appStartResponseBody, Response response) {
                    int excSize = appStartResponseBody.getMyExceptions().size();
                    if (excSize > 0) {
                        ExceptionManager.getInstance().addExceptions(appStartResponseBody.getMyExceptions());
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



    public String getAndroidId() { return androidId; }
}
