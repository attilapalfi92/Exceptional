package com.attilapalf.exceptional.rest.messages;

import java.util.Collection;
import java.util.List;

/**
 * Created by 212461305 on 2015.07.04..
 */
public class BaseRequestBody {
    protected long userId;
    protected List<Long> exceptionIds;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public List<Long> getExceptionIds() {
        return exceptionIds;
    }

    public void setExceptionIds(List<Long> exceptionIds) {
        this.exceptionIds = exceptionIds;
    }
}
