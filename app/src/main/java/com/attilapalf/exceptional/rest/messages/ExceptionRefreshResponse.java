package com.attilapalf.exceptional.rest.messages;

import java.util.List;

/**
 * Created by 212461305 on 2015.07.04..
 */
public class ExceptionRefreshResponse {
    List<ExceptionInstanceWrapper> neededExceptions;

    public ExceptionRefreshResponse() {
    }

    public ExceptionRefreshResponse(List<ExceptionInstanceWrapper> neededExceptions) {
        this.neededExceptions = neededExceptions;
    }

    public List<ExceptionInstanceWrapper> getNeededExceptions() {
        return neededExceptions;
    }

    public void setNeededExceptions(List<ExceptionInstanceWrapper> neededExceptions) {
        this.neededExceptions = neededExceptions;
    }
}
