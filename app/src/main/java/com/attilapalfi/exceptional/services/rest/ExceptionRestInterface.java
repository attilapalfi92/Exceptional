package com.attilapalfi.exceptional.services.rest;

import com.attilapalfi.exceptional.services.rest.messages.BaseExceptionRequest;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionInstanceWrapper;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionRefreshResponse;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionSentResponse;
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
}
