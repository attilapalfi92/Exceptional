package com.attilapalfi.exceptional.services.rest.messages;

import java.math.BigInteger;

/**
 * Created by palfi on 2015-09-06.
 */
public class VoteRequest {
    private String userId;
    private int votedExceptionId;

    public VoteRequest( ) {
    }

    public VoteRequest( String userId, int votedExceptionId ) {
        this.userId = userId;
        this.votedExceptionId = votedExceptionId;
    }

    public String getUserId( ) {
        return userId;
    }

    public void setUserId( String userId ) {
        this.userId = userId;
    }

    public int getVotedExceptionId( ) {
        return votedExceptionId;
    }

    public void setVotedExceptionId( int votedExceptionId ) {
        this.votedExceptionId = votedExceptionId;
    }
}
