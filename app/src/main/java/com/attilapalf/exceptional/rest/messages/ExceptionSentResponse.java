package com.attilapalf.exceptional.rest.messages;

/**
 * Created by Attila on 2015-06-21.
 */
public class ExceptionSentResponse {
    private long toWho;
    private String shortName;

    public ExceptionSentResponse() {
    }

    public ExceptionSentResponse(long toWho, String shortName) {
        this.toWho = toWho;
        this.shortName = shortName;
    }

    public long getToWho() {
        return toWho;
    }

    public void setToWho(long toWho) {
        this.toWho = toWho;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
