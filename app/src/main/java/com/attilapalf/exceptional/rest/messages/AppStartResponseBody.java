package com.attilapalf.exceptional.rest.messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Attila on 2015-06-11.
 */
public class AppStartResponseBody {
    private List<ExceptionWrapper> myExceptions;

    public AppStartResponseBody() {
        myExceptions = new ArrayList<>();
    }

    public List<ExceptionWrapper> getMyExceptions() {
        return myExceptions;
    }

    public void setMyExceptions(List<ExceptionWrapper> myExceptions) {
        this.myExceptions = myExceptions;
    }
}
