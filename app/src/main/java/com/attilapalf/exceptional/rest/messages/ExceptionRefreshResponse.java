package com.attilapalf.exceptional.rest.messages;

import com.attilapalf.exceptional.model.Exception;

import java.util.List;

/**
 * Created by 212461305 on 2015.07.04..
 */
public class ExceptionRefreshResponse {
    List<ExceptionWrapper> neededExceptions;

    public ExceptionRefreshResponse() {
    }

    public ExceptionRefreshResponse(List<ExceptionWrapper> neededExceptions) {
        this.neededExceptions = neededExceptions;
    }

    public List<ExceptionWrapper> getNeededExceptions() {
        return neededExceptions;
    }

    public void setNeededExceptions(List<ExceptionWrapper> neededExceptions) {
        this.neededExceptions = neededExceptions;
    }
}
