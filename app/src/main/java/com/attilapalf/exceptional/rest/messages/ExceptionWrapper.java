package com.attilapalf.exceptional.rest.messages;


import com.attilapalf.exceptional.model.Exception;


/**
 * Created by Attila on 2015-06-11.
 */
public class ExceptionWrapper {
    private long fromWho, toWho;
    private long timeInMillis;
    private double longitude, latitude;
    private int exceptionTypeId;
    private long instanceId;

    public ExceptionWrapper() {
    }

    public ExceptionWrapper(Exception e) {
        fromWho = e.getFromWho();
        toWho = e.getToWho();
        timeInMillis = e.getDate().getTime();
        longitude = e.getLongitude();
        latitude = e.getLatitude();
        exceptionTypeId = e.getExceptionTypeId();
        instanceId = e.getInstanceId();
    }

    public ExceptionWrapper(Long fromWho, Long toWho, long timeInMillis,
                            double longitude, double latitude, int exceptionTypeId, long instanceId) {
        this.fromWho = fromWho;
        this.toWho = toWho;
        this.timeInMillis = timeInMillis;
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

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
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