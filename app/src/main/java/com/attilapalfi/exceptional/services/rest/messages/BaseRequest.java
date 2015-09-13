package com.attilapalfi.exceptional.services.rest.messages;

import java.math.BigInteger;

/**
 * Created by 212461305 on 2015.07.11..
 */
public abstract class BaseRequest {
    protected String userFacebookId;

    public String getUserFacebookId( ) {
        return userFacebookId;
    }

    public void setUserFacebookId( String userFacebookId ) {
        this.userFacebookId = userFacebookId;
    }
}
