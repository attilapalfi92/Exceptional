package com.attilapalf.exceptional.rest.messages;

import com.attilapalf.exceptional.model.*;
import com.attilapalf.exceptional.model.Exception;

import java.util.List;

/**
 * Created by 212461305 on 2015.07.04..
 */
public class ExceptionRefreshResponse {
    List<Exception> latestExceptions;
    long nextExceptionInstanceId;

    public ExceptionRefreshResponse() {
    }

    public ExceptionRefreshResponse(List<Exception> latestExceptions, long nextExceptionInstanceId) {
        this.latestExceptions = latestExceptions;
        this.nextExceptionInstanceId = nextExceptionInstanceId;
    }

    public List<Exception> getLatestExceptions() {
        return latestExceptions;
    }

    public void setLatestExceptions(List<Exception> latestExceptions) {
        this.latestExceptions = latestExceptions;
    }

    public long getNextExceptionInstanceId() {
        return nextExceptionInstanceId;
    }

    public void setNextExceptionInstanceId(long nextExceptionInstanceId) {
        this.nextExceptionInstanceId = nextExceptionInstanceId;
    }
}
