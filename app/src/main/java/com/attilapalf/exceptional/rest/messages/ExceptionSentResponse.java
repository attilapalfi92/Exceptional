package com.attilapalf.exceptional.rest.messages;

import java.math.BigInteger;

/**
 * Created by Attila on 2015-06-21.
 */
public class ExceptionSentResponse {
    private BigInteger toWho;
    private String shortName;
    private int points;

    public ExceptionSentResponse() {
    }

    public ExceptionSentResponse(BigInteger toWho, String shortName, int points) {
        this.toWho = toWho;
        this.shortName = shortName;
        this.points = points;
    }

    public BigInteger getToWho() {
        return toWho;
    }

    public void setToWho(BigInteger toWho) {
        this.toWho = toWho;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
