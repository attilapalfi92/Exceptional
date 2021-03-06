package com.attilapalfi.exceptional.rest;

import com.attilapalf.exceptional.messages.QuestionAnswer;
import com.attilapalfi.exceptional.rest.messages.BaseExceptionRequest;
import com.attilapalfi.exceptional.rest.messages.ExceptionInstanceWrapper;
import com.attilapalfi.exceptional.rest.messages.ExceptionRefreshResponse;
import com.attilapalfi.exceptional.rest.messages.ExceptionSentResponse;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by palfi on 2015-09-13.
 */
public interface ExceptionRestInterface {
    @POST( "/exception" )
    void throwException( @Body ExceptionInstanceWrapper exceptionInstanceWrapper, Callback<ExceptionSentResponse> cb );

    @POST( "/exception/refresh" )
    void refreshExceptions( @Body BaseExceptionRequest requestBody, Callback<ExceptionRefreshResponse> cb );

    @POST( "/exception/answer" )
    void answerQuestion( @Body QuestionAnswer questionAnswer, Callback<ExceptionSentResponse> cb );
}
