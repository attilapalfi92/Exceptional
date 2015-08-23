package com.attilapalfi.exceptional.rest.messages;

import com.attilapalfi.exceptional.model.ExceptionType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Attila on 2015-06-11.
 */
public class AppStartResponseBody {
    private List<ExceptionInstanceWrapper> myExceptions;
    private List<ExceptionType> exceptionTypes;
    private List<ExceptionType> beingVotedTypes;
    private Map<BigInteger, Integer> friendsPoints;
    private int points;
    private int exceptionVersion;

    public AppStartResponseBody() {
        myExceptions = new ArrayList<>();
        exceptionTypes = new ArrayList<>();
        beingVotedTypes = new ArrayList<>();
        friendsPoints = new HashMap<>();
    }

    public List<ExceptionInstanceWrapper> getMyExceptions() {
        return myExceptions;
    }

    public void setMyExceptions(List<ExceptionInstanceWrapper> myExceptions) {
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

    public Map<BigInteger, Integer> getFriendsPoints() {
        return friendsPoints;
    }

    public void setFriendsPoints(Map<BigInteger, Integer> friendsPoints) {
        this.friendsPoints = friendsPoints;
    }
}
