package com.attilapalf.exceptional.rest;

import com.attilapalf.exceptional.rest.messages.AppStartRequestBody;
import com.attilapalf.exceptional.rest.messages.AppStartResponseBody;
import com.attilapalf.exceptional.rest.messages.BaseExceptionRequestBody;
import com.attilapalf.exceptional.rest.messages.ExceptionRefreshResponse;
import com.attilapalf.exceptional.rest.messages.ExceptionSentResponse;
import com.attilapalf.exceptional.rest.messages.ExceptionInstanceWrapper;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by palfi on 2015-08-19.
 */
public interface RestInterface {
    @POST("/application/firstAppStart")
    void firstAppStart(@Body AppStartRequestBody requestBody, Callback<AppStartResponseBody> cb);

    @POST("/application/regularAppStart")
    void regularAppStart(@Body AppStartRequestBody requestBody, Callback<AppStartResponseBody> cb);

    @POST("/exception")
    void sendException(@Body ExceptionInstanceWrapper exceptionInstanceWrapper, Callback<ExceptionSentResponse> cb);

    @POST("/exception/refresh")
    void refreshExceptions(@Body BaseExceptionRequestBody requestBody, Callback<ExceptionRefreshResponse> cb);
}
