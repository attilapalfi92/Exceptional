package com.attilapalf.exceptional.rest;

import com.attilapalf.exceptional.model.*;
import com.attilapalf.exceptional.model.Exception;
import com.attilapalf.exceptional.ui.main.ExceptionChangeListener;
import com.attilapalf.exceptional.ui.main.ExceptionSource;
import com.attilapalf.exceptional.ui.main.FriendChangeListener;
import com.attilapalf.exceptional.ui.main.FriendSource;
import com.attilapalf.exceptional.utils.ExceptionFactory;
import com.attilapalf.exceptional.utils.ExceptionManager;
import com.attilapalf.exceptional.utils.FacebookManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.Body;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;

/**
 * Created by Attila on 2015-06-13.
 */
public class BackendConnector implements BackendService, ExceptionSource, FriendSource,
        ConnectionFailedSource {

    private String androidId;
    private Gson gson;
    private RestAdapter restAdapter;
    private RestInterface restInterface;
    private Set<ConnectionFailedListener> connectionListeners;
    private Set<ExceptionChangeListener> exceptionChangeListeners;
    private Set<FriendChangeListener> friendChangeListeners;

    private static BackendConnector instance;

    public static BackendConnector getInstance() {
        if (instance == null) {
            instance = new BackendConnector();
        }

        return instance;
    }

    public BackendConnector setAndroidId(String aId) {
        androidId = aId;
        return this;
    }

    private BackendConnector(){
        connectionListeners = new HashSet<>();
        exceptionChangeListeners = new HashSet<>();

        gson = new GsonBuilder()
                .registerTypeAdapter(Calendar.class, new DateTypeAdapter())
                .create();

        restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://188.36.23.85:8090")
                //.setEndpoint("http://localhost:8090")
                .setConverter(new GsonConverter((gson)))
                .build();

        restInterface = restAdapter.create(RestInterface.class);
    }

    @Override
    public boolean addConnectionListener(ConnectionFailedListener listener) {
        return connectionListeners.add(listener);
    }

    @Override
    public boolean removeConnectionListener(ConnectionFailedListener listener) {
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
        @Headers({
                "Accept: application/json"
        })
        @POST("/user/firstAppStart")
        void firstAppStart(@Body AppStartRequestBody requestBody, Callback<AppStartResponseBody> cb);
//        @POST("/user/firstAppStart")
//        AppStartResponseBody firstAppStart(@Body AppStartRequestBody requestBody);
    }


//    @Override
//    public void onFirstAppStart(Set<Friend> friendSet) {
//        long userId = FacebookManager.getProfileId();
//        List<Long> friendIdList = new ArrayList<>(friendSet.size());
//        for(Friend f : friendSet) {
//            friendIdList.add(f.getId());
//        }
//        AppStartRequestBody requestBody = new AppStartRequestBody(androidId, userId, friendIdList);
//
//        try {
//            AppStartResponseBody appStartResponseBody = restInterface.firstAppStart(requestBody);
//
//            ExceptionManager.saveStarterId(appStartResponseBody.getExceptionIdStarter());
//            int excSize = appStartResponseBody.getMyExceptions().size();
//            if (excSize > 0) {
//                for (int i = 0; i < excSize; i++) {
//                    ExceptionWrapper eW = appStartResponseBody.getMyExceptions().get(i);
//                    Exception e = new Exception();
//                    e.setExceptionType(ExceptionFactory.findById(eW.exceptionTypeId));
//                    e.setFromWho(eW.getFromWho());
//                    e.setToWho(eW.getToWho());
//                    e.setDate(eW.getCreationDate());
//                    e.setInstanceId(eW.getInstanceId());
//                    ExceptionManager.addException(e);
//                }
//                for (ExceptionChangeListener l : exceptionChangeListeners) {
//                    l.onExceptionsChanged();
//                }
//            }
//        } catch (java.lang.Exception e) {
//            e.printStackTrace();
//        }
//
//    }



    @Override
    public void onFirstAppStart(Set<Friend> friendSet) {
        long userId = FacebookManager.getProfileId();
        List<Long> friendIdList = new ArrayList<>(friendSet.size());
        for(Friend f : friendSet) {
            friendIdList.add(f.getId());
        }
        AppStartRequestBody requestBody = new AppStartRequestBody(androidId, userId, friendIdList);

        try {
            restInterface.firstAppStart(requestBody, new Callback<AppStartResponseBody>() {
                @Override
                public void success(AppStartResponseBody appStartResponseBody, Response response) {
                    ExceptionManager.saveStarterId(appStartResponseBody.getExceptionIdStarter());
                    int excSize = appStartResponseBody.getMyExceptions().size();
                    if (excSize > 0) {
                        for (int i = 0; i < excSize; i++) {
                            ExceptionWrapper eW = appStartResponseBody.getMyExceptions().get(i);
                            Exception e = new Exception();
                            e.setExceptionType(ExceptionFactory.findById(eW.exceptionTypeId));
                            e.setFromWho(eW.getFromWho());
                            e.setToWho(eW.getToWho());
                            e.setDate(eW.getCreationDate());
                            e.setInstanceId(eW.getInstanceId());
                            ExceptionManager.addException(e);
                        }
                        for(ExceptionChangeListener l : exceptionChangeListeners) {
                            l.onExceptionsChanged();
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    for(ConnectionFailedListener l : connectionListeners) {
                        l.onConnectionFailed("Connection to server failed.", error.getMessage());
                    }
                }
            });
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onAppStart(Set<Friend> friendSet) {

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
