package com.attilapalfi.exceptional.services.rest;

import com.attilapalfi.exceptional.services.rest.messages.AppStartRequestBody;
import com.attilapalfi.exceptional.services.rest.messages.AppStartResponseBody;
import com.attilapalfi.exceptional.services.rest.messages.BaseExceptionRequestBody;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionRefreshResponse;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionSentResponse;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionInstanceWrapper;

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
    void throwException(@Body ExceptionInstanceWrapper exceptionInstanceWrapper, Callback<ExceptionSentResponse> cb);

    @POST("/exception/refresh")
    void refreshExceptions(@Body BaseExceptionRequestBody requestBody, Callback<ExceptionRefreshResponse> cb);
}
