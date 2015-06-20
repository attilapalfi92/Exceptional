package com.attilapalf.exceptional.rest;


import java.util.Calendar;

/**
 * Created by Attila on 2015-06-11.
 */
public class ExceptionWrapper {
    private long fromWho, toWho;
    private Calendar creationDate;
    double longitude, latitude;
    int exceptionTypeId;
    long instanceId;

    public ExceptionWrapper() {
    }

    public ExceptionWrapper(Long fromWho, Long toWho, Calendar creationDate,
                            double longitude, double latitude, int exceptionTypeId, long instanceId) {
        this.fromWho = fromWho;
        this.toWho = toWho;
        this.creationDate = creationDate;
        this.longitude = longitude;
        this.latitude = latitude;
        this.exceptionTypeId = exceptionTypeId;
        this.instanceId = instanceId;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    public long getFromWho() {
        return fromWho;
    }

    public void setFromWho(long fromWho) {
        this.fromWho = fromWho;
    }

    public long getToWho() {
        return toWho;
    }

    public void setToWho(long toWho) {
        this.toWho = toWho;
    }

    public Calendar getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Calendar creationDate) {
        this.creationDate = creationDate;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getExceptionTypeId() {
        return exceptionTypeId;
    }

    public void setExceptionTypeId(int exceptionTypeId) {
        this.exceptionTypeId = exceptionTypeId;
    }
}
