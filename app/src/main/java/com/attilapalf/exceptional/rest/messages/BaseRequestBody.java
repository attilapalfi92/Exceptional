package com.attilapalf.exceptional.rest.messages;

/**
 * Created by 212461305 on 2015.07.11..
 */
public abstract class BaseRequestBody {
    protected long userId;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
