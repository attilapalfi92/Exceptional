package com.attilapalf.exceptional.rest;

import android.content.Context;
import android.os.AsyncTask;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.*;
import com.attilapalf.exceptional.model.Exception;
import com.attilapalf.exceptional.rest.messages.AppStartRequestBody;
import com.attilapalf.exceptional.rest.messages.AppStartResponseBody;
import com.attilapalf.exceptional.rest.messages.BaseRequestBody;
import com.attilapalf.exceptional.rest.messages.ExceptionRefreshResponse;
import com.attilapalf.exceptional.rest.messages.ExceptionSentResponse;
import com.attilapalf.exceptional.rest.messages.ExceptionWrapper;
import com.attilapalf.exceptional.ui.main.ExceptionChangeListener;
import com.attilapalf.exceptional.ui.main.ExceptionSource;
import com.attilapalf.exceptional.ui.main.FriendChangeListener;
import com.attilapalf.exceptional.ui.main.FriendSource;
import com.attilapalf.exceptional.utils.ExceptionFactory;
import com.attilapalf.exceptional.utils.ExceptionManager;
import com.attilapalf.exceptional.utils.FacebookManager;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.sql.Timestamp;
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
import retrofit.http.GET;
import retrofit.http.POST;

/**
 * Created by Attila on 2015-06-13.
 */
public class BackendConnector implements BackendService, ExceptionSource, FriendSource,
        ServerResponseSource {

    private final Context context;
    private final String projectNumber;
    private GoogleCloudMessaging googleCloudMessaging;
    private String registrationId;

    private String androidId;
    private Gson gson;
    private RestAdapter restAdapter;
    private RestInterface restInterface;
    private Set<ServerResponseListener> connectionListeners;
    private Set<ExceptionChangeListener> exceptionChangeListeners;
    private Set<FriendChangeListener> friendChangeListeners;

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

        connectionListeners = new HashSet<>();
        exceptionChangeListeners = new HashSet<>();
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
        return connectionListeners.add(listener);
    }

    @Override
    public boolean removeConnectionListener(ServerResponseListener listener) {
        return connectionListeners.remove(listener);
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

        @GET("/exception")
        void refreshExceptions(@Body BaseRequestBody requestBody, Callback<ExceptionRefreshResponse> cb);
    }



    public void refreshExceptions() {
        try {
            // TODO: finish
            //restInterface.refreshExceptions();
        } catch (java.lang.Exception e) {

        }
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
                    ExceptionManager.getInstance().saveStarterId(appStartResponseBody.getExceptionIdStarter());
                    int excSize = appStartResponseBody.getMyExceptions().size();
                    if (excSize > 0) {
                        for (int i = 0; i < excSize; i++) {
                            ExceptionWrapper eW = appStartResponseBody.getMyExceptions().get(i);
                            Exception e = new Exception();
                            e.setExceptionType(ExceptionFactory.findById(eW.getExceptionTypeId()));
                            e.setFromWho(eW.getFromWho());
                            e.setToWho(eW.getToWho());
                            e.setDate(new Timestamp(eW.getTimeInMillis()));
                            e.setInstanceId(eW.getInstanceId());
                            ExceptionManager.getInstance().addException(e);
                        }
                        for (ExceptionChangeListener l : exceptionChangeListeners) {
                            l.onExceptionsChanged();
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    for (ServerResponseListener l : connectionListeners) {
                        l.onConnectionFailed("Connection to server failed.", error.getMessage());
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

                for(ServerResponseListener l : connectionListeners) {
                    l.onSuccess(message);
                }
            }

        }.execute(null, null, null);
    }



    private void backendFirstAppStart() {
        try {
            restInterface.firstAppStart(requestBody, new Callback<AppStartResponseBody>() {
                @Override
                public void success(AppStartResponseBody appStartResponseBody, Response response) {
                    ExceptionManager.getInstance().saveStarterId(appStartResponseBody.getExceptionIdStarter());
                    int excSize = appStartResponseBody.getMyExceptions().size();
                    if (excSize > 0) {
                        for (int i = 0; i < excSize; i++) {
                            ExceptionWrapper eW = appStartResponseBody.getMyExceptions().get(i);
                            Exception e = new Exception();
                            e.setExceptionType(ExceptionFactory.findById(eW.getExceptionTypeId()));
                            e.setFromWho(eW.getFromWho());
                            e.setToWho(eW.getToWho());
                            e.setDate(new Timestamp(eW.getTimeInMillis()));
                            e.setInstanceId(eW.getInstanceId());
                            ExceptionManager.getInstance().addException(e);
                        }
                        for (ExceptionChangeListener l : exceptionChangeListeners) {
                            l.onExceptionsChanged();
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    for (ServerResponseListener l : connectionListeners) {
                        l.onConnectionFailed("Connection to server failed.", error.getMessage());
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
                    for (ServerResponseListener l : connectionListeners) {
                        l.onSuccess(e.getShortName() + " successfully sent to " + e.getToWho());
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    for (ServerResponseListener l : connectionListeners) {
                        l.onConnectionFailed("Failed to send the exception to the server.", error.getMessage());
                    }
                }
            });

        } catch (java.lang.Exception exception) {

        }
    }


    @Override
    public boolean addExceptionChangeListener(ExceptionChangeListener listener) {
        if (exceptionChangeListeners == null) {
            exceptionChangeListeners = new HashSet<>();
        }
        return exceptionChangeListeners.add(listener);
    }

    @Override
    public boolean removeExceptionChangeListener(ExceptionChangeListener listener) {
        if (exceptionChangeListeners == null) {
            exceptionChangeListeners = new HashSet<>();
        }
        return exceptionChangeListeners.remove(listener);
    }

    public String getAndroidId() { return androidId; }
}
