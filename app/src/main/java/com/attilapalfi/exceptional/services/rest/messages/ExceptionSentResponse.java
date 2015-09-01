package com.attilapalfi.exceptional.services.rest.messages;

import java.math.BigInteger;

/**
 * Created by Attila on 2015-06-21.
 */
public class ExceptionSentResponse {
    private String exceptionShortName;
    private int yourPoints;
    private int friendsPoints;
    private ExceptionInstanceWrapper instanceWrapper;

    public ExceptionSentResponse() {
    }

    public ExceptionSentResponse(ExceptionInstanceWrapper instanceWrapper,
                                 String exceptionShortName, int yourPoints, int friendsPoints) {
        this.instanceWrapper = instanceWrapper;
        this.exceptionShortName = exceptionShortName;
        this.yourPoints = yourPoints;
        this.friendsPoints = friendsPoints;
    }

    public String getExceptionShortName() {
        return exceptionShortName;
    }

    public void setExceptionShortName(String exceptionShortName) {
        this.exceptionShortName = exceptionShortName;
    }

    public int getYourPoints() {
        return yourPoints;
    }

    public void setYourPoints(int yourPoints) {
        this.yourPoints = yourPoints;
    }

    public int getFriendsPoints() {
        return friendsPoints;
    }

    public void setFriendsPoints(int friendsPoints) {
        this.friendsPoints = friendsPoints;
    }

    public ExceptionInstanceWrapper getInstanceWrapper() {
        return instanceWrapper;
    }

    public void setInstanceWrapper(ExceptionInstanceWrapper instanceWrapper) {
        this.instanceWrapper = instanceWrapper;
    }
}
