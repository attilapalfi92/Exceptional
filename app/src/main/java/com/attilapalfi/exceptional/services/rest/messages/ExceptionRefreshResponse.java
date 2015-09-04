package com.attilapalfi.exceptional.services.rest.messages;

import java.util.List;

/**
 * Created by 212461305 on 2015.07.04..
 */
public class ExceptionRefreshResponse {
    List<ExceptionInstanceWrapper> exceptionList;

    public ExceptionRefreshResponse() {
    }

    public ExceptionRefreshResponse(List<ExceptionInstanceWrapper> exceptionList) {
        this.exceptionList = exceptionList;
    }

    public List<ExceptionInstanceWrapper> getExceptionList() {
        return exceptionList;
    }

    public void setExceptionList(List<ExceptionInstanceWrapper> exceptionList) {
        this.exceptionList = exceptionList;
    }
}
