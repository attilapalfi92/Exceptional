package com.attilapalfi.exceptional.services.rest;

import com.attilapalfi.exceptional.services.rest.messages.SubmitRequest;
import com.attilapalfi.exceptional.services.rest.messages.AppStartRequest;
import com.attilapalfi.exceptional.services.rest.messages.AppStartResponse;
import com.attilapalfi.exceptional.services.rest.messages.BaseExceptionRequest;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionRefreshResponse;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionSentResponse;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionInstanceWrapper;
import com.attilapalfi.exceptional.services.rest.messages.SubmitResponse;
import com.attilapalfi.exceptional.services.rest.messages.VoteRequest;
import com.attilapalfi.exceptional.services.rest.messages.VoteResponse;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by palfi on 2015-08-19.
 */
public interface RestInterface {
    @POST("/application/firstAppStart")
    void firstAppStart(@Body AppStartRequest requestBody, Callback<AppStartResponse> cb);

    @POST("/application/regularAppStart")
    void regularAppStart(@Body AppStartRequest requestBody, Callback<AppStartResponse> cb);

    @POST("/exception")
    void throwException(@Body ExceptionInstanceWrapper exceptionInstanceWrapper, Callback<ExceptionSentResponse> cb);

    @POST("/exception/refresh")
    void refreshExceptions(@Body BaseExceptionRequest requestBody, Callback<ExceptionRefreshResponse> cb);

    @POST("/voting/vote")
    void voteForType(@Body VoteRequest request, Callback<VoteResponse> cb);

    @POST("/voting/submit")
    void submitTypeForVote(@Body SubmitRequest request, Callback<SubmitResponse> cb);
}
