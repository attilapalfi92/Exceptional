package com.attilapalf.exceptional.rest.messages;

import java.math.BigInteger;

/**
 * Created by Attila on 2015-06-21.
 */
public class ExceptionSentResponse {
    private BigInteger toWho;
    private String shortName;

    public ExceptionSentResponse() {
    }

    public ExceptionSentResponse(BigInteger toWho, String shortName) {
        this.toWho = toWho;
        this.shortName = shortName;
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
}
