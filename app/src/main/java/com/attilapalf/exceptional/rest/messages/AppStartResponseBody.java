package com.attilapalf.exceptional.rest.messages;

import com.attilapalf.exceptional.model.ExceptionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Attila on 2015-06-11.
 */
public class AppStartResponseBody {
    private List<ExceptionWrapper> myExceptions;
    private List<ExceptionType> exceptionTypes;
    private List<ExceptionType> beingVotedTypes;
    private int points;
    private int exceptionVersion;

    public AppStartResponseBody() {
        myExceptions = new ArrayList<>();
        myExceptions = new ArrayList<>();
        exceptionTypes = new ArrayList<>();
        beingVotedTypes = new ArrayList<>();
    }

    public List<ExceptionWrapper> getMyExceptions() {
        return myExceptions;
    }

    public void setMyExceptions(List<ExceptionWrapper> myExceptions) {
        this.myExceptions = myExceptions;
    }

    public List<ExceptionType> getExceptionTypes() {
        return exceptionTypes;
    }

    public void setExceptionTypes(List<ExceptionType> exceptionTypes) {
        this.exceptionTypes = exceptionTypes;
    }

    public List<ExceptionType> getBeingVotedTypes() {
        return beingVotedTypes;
    }

    public void setBeingVotedTypes(List<ExceptionType> beingVotedTypes) {
        this.beingVotedTypes = beingVotedTypes;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getExceptionVersion() {
        return exceptionVersion;
    }

    public void setExceptionVersion(int exceptionVersion) {
        this.exceptionVersion = exceptionVersion;
    }
}
