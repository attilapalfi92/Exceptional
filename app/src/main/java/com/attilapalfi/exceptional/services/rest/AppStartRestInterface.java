package com.attilapalfi.exceptional.services.rest;

import com.attilapalfi.exceptional.services.rest.messages.*;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by palfi on 2015-08-19.
 */
public interface AppStartRestInterface {
    @POST( "/application/firstAppStart" )
    void firstAppStart( @Body AppStartRequest requestBody, Callback<AppStartResponse> cb );

    @POST( "/application/regularAppStart" )
    void regularAppStart( @Body AppStartRequest requestBody, Callback<AppStartResponse> cb );
}
