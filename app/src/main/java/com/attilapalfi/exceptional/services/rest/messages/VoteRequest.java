package com.attilapalfi.exceptional.services.rest.messages;

import java.math.BigInteger;

/**
 * Created by palfi on 2015-09-06.
 */
public class VoteRequest {
    private BigInteger userId;
    private int votedExceptionId;

    public VoteRequest() {
    }

    public VoteRequest(BigInteger userId, int votedExceptionId) {
        this.userId = userId;
        this.votedExceptionId = votedExceptionId;
    }

    public BigInteger getUserId() {
        return userId;
    }

    public void setUserId(BigInteger userId) {
        this.userId = userId;
    }

    public int getVotedExceptionId() {
        return votedExceptionId;
    }

    public void setVotedExceptionId(int votedExceptionId) {
        this.votedExceptionId = votedExceptionId;
    }
}
