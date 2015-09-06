package com.attilapalfi.exceptional.services.rest;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;

import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.model.*;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.services.rest.messages.AppStartRequest;
import com.attilapalfi.exceptional.services.rest.messages.AppStartResponse;
import com.attilapalfi.exceptional.services.rest.messages.BaseExceptionRequest;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionRefreshResponse;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionSentResponse;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionInstanceWrapper;
import com.attilapalfi.exceptional.interfaces.ExceptionRefreshListener;
import com.attilapalfi.exceptional.services.Converter;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionInstanceManager;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionTypeManager;
import com.attilapalfi.exceptional.services.persistent_stores.FriendsManager;
import com.attilapalfi.exceptional.services.persistent_stores.MetadataStore;
import com.attilapalfi.exceptional.services.facebook.FacebookManager;
import com.attilapalfi.exceptional.services.rest.messages.SubmitRequest;
import com.attilapalfi.exceptional.services.rest.messages.SubmitResponse;
import com.attilapalfi.exceptional.services.rest.messages.VoteRequest;
import com.attilapalfi.exceptional.services.rest.messages.VoteResponse;
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
public class BackendService {

    private static BackendService instance;
    private final Context context;
    private final String projectNumber;

    private GoogleCloudMessaging googleCloudMessaging;
    private String registrationId;
    private String androidId;
    private RestInterface restInterface;
    private AppStartRequest requestBody = new AppStartRequest();

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
            restInterface.regularAppStart(requestBody, new Callback<AppStartResponse>() {
                @Override
                public void success(AppStartResponse responseBody, Response response) {
                    saveCommonData(responseBody);
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(context, context.getString(R.string.failed_to_connect) + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

    public void throwException(Exception exception) {
        ExceptionInstanceWrapper exceptionInstanceWrapper = new ExceptionInstanceWrapper(exception);
        try{
            restInterface.throwException(exceptionInstanceWrapper, new Callback<ExceptionSentResponse>() {
                @Override
                public void success(ExceptionSentResponse e, Response response) {
                    Friend toWho = FriendsManager.getInstance().findFriendById(e.getInstanceWrapper().getToWho());
                    MetadataStore.getInstance().setPoints(e.getYourPoints());
                    FriendsManager.getInstance().updateFriendPoints(e.getInstanceWrapper().getToWho(), e.getFriendsPoints());
                    ExceptionInstanceManager.getInstance().addException(new Exception(e.getInstanceWrapper()));
                    Toast.makeText(context, e.getExceptionShortName() + " "
                                    + context.getString(R.string.successfully_thrown)
                                    + " " + toWho.getName(),
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(context, context.getString(R.string.failed_to_throw_1) + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

        } catch (java.lang.Exception e) {
            Toast.makeText(context, context.getString(R.string.failed_to_throw_2) + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void refreshExceptions(final ExceptionRefreshListener refreshListener) {
        BaseExceptionRequest requestBody = new BaseExceptionRequest(
                FacebookManager.getInstance().getProfileId(),
                ExceptionInstanceManager.getInstance().getExceptionList()
        );
        restInterface.refreshExceptions(requestBody, new Callback<ExceptionRefreshResponse>() {

            @Override
            public void success(ExceptionRefreshResponse exceptionRefreshResponse, Response response) {
                ExceptionInstanceManager.getInstance().saveExceptions(exceptionRefreshResponse.getExceptionList());
                Toast.makeText(context, R.string.exceptions_syncd, Toast.LENGTH_SHORT).show();
                refreshListener.onExceptionRefreshFinished();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(context, context.getString(R.string.failed_to_sync) + error.getMessage(), Toast.LENGTH_SHORT).show();
                refreshListener.onExceptionRefreshFinished();
            }
        });
    }

    public void voteForType(ExceptionType exceptionType) {
        VoteRequest voteRequest = new VoteRequest(FriendsManager.getInstance().getYourself().getId(), exceptionType.getId());
        restInterface.voteForType(voteRequest, new Callback<VoteResponse>() {
            @Override
            public void success(VoteResponse voteResponse, Response response) {
                if (voteResponse.isVotedForThisWeek()) {
                    MetadataStore.getInstance().setVotedThisWeek(true);
                    ExceptionTypeManager.getInstance().updateVotedType(voteResponse.getVotedType());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(context, R.string.failed_to_vote, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void submitType(ExceptionType submittedType) {
        SubmitRequest submitRequest = new SubmitRequest(FriendsManager.getInstance().getYourself().getId(), submittedType);
        restInterface.submitTypeForVote(submitRequest, new Callback<SubmitResponse>() {
            @Override
            public void success(SubmitResponse submitResponse, Response response) {
                if (submitResponse.isSubmittedThisWeek()) {
                    MetadataStore.getInstance().setSubmittedThisWeek(true);
                    ExceptionTypeManager.getInstance().addVotedType(submitResponse.getSubmittedType());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(context, R.string.failed_to_submit, Toast.LENGTH_SHORT).show();
            }
        });
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
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }

        }.execute();
    }

    private void backendFirstAppStart() {
        try {
            restInterface.firstAppStart(requestBody, new Callback<AppStartResponse>() {
                @Override
                public void success(AppStartResponse responseBody, Response response) {
                    saveCommonData(responseBody);
                    MetadataStore.getInstance().setFirstStartFinished(true);
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(context, context.getString(R.string.failed_to_connect_3) + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

    private void saveCommonData(AppStartResponse responseBody) {
        if (responseBody.getExceptionVersion() > MetadataStore.getInstance().getExceptionVersion()) {
            ExceptionTypeManager.getInstance().addExceptionTypes(responseBody.getExceptionTypes());
        }
        MetadataStore.getInstance().setExceptionVersion(responseBody.getExceptionVersion());
        ExceptionInstanceManager.getInstance().saveExceptions(responseBody.getMyExceptions());
        ExceptionTypeManager.getInstance().setVotedExceptionTypes(responseBody.getBeingVotedTypes());
        MetadataStore.getInstance().setPoints(responseBody.getPoints());
        MetadataStore.getInstance().setSubmittedThisWeek(responseBody.isSubmittedThisWeek());
        MetadataStore.getInstance().setVotedThisWeek(responseBody.isVotedThisWeek());
        FriendsManager.getInstance().updateFriendsPoints(responseBody.getFriendsPoints());
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
