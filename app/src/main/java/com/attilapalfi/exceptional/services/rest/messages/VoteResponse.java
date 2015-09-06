package com.attilapalfi.exceptional.services.rest.messages;

import com.attilapalfi.exceptional.model.ExceptionType;

/**
 * Created by palfi on 2015-09-06.
 */
public class VoteResponse {
    private boolean votedForThisWeek;
    private ExceptionType votedType;

    public VoteResponse() {
    }

    public VoteResponse(boolean votedForThisWeek, ExceptionType votedType) {
        this.votedForThisWeek = votedForThisWeek;
        this.votedType = votedType;
    }

    public boolean isVotedForThisWeek() {
        return votedForThisWeek;
    }

    public void setVotedForThisWeek(boolean votedForThisWeek) {
        this.votedForThisWeek = votedForThisWeek;
    }

    public ExceptionType getVotedType() {
        return votedType;
    }

    public void setVotedType(ExceptionType votedType) {
        this.votedType = votedType;
    }
}
