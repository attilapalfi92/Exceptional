package com.attilapalf.exceptional.rest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Attila on 2015-06-11.
 */
public class AppStartResponseBody {
    private List<ExceptionWrapper> myExceptions;
    private long exceptionIdStarter;

    public AppStartResponseBody() {
        myExceptions = new ArrayList<>();
    }

    public long getExceptionIdStarter() {
        return exceptionIdStarter;
    }

    public void setExceptionIdStarter(long exceptionIdStarter) {
        this.exceptionIdStarter = exceptionIdStarter;
    }

    public List<ExceptionWrapper> getMyExceptions() {
        return myExceptions;
    }

    public void setMyExceptions(List<ExceptionWrapper> myExceptions) {
        this.myExceptions = myExceptions;
    }
}
