package com.attilapalfi.exceptional.services.rest.messages;


import java.math.BigInteger;

import com.attilapalfi.exceptional.model.Exception;


/**
 * Created by Attila on 2015-06-11.
 */
public class ExceptionInstanceWrapper {
    private String fromWho, toWho;
    private long timeInMillis;
    private double longitude, latitude;
    private int exceptionTypeId;
    private BigInteger instanceId;

    public ExceptionInstanceWrapper( ) {
    }

    public ExceptionInstanceWrapper( Exception e ) {
        fromWho = e.getFromWho();
        toWho = e.getToWho();
        timeInMillis = e.getDate().getTime();
        longitude = e.getLongitude();
        latitude = e.getLatitude();
        exceptionTypeId = e.getExceptionTypeId();
        instanceId = e.getInstanceId();
    }

    public ExceptionInstanceWrapper( String fromWho, String toWho, long timeInMillis,
                                     double longitude, double latitude, int exceptionTypeId, BigInteger instanceId ) {
        this.fromWho = fromWho;
        this.toWho = toWho;
        this.timeInMillis = timeInMillis;
        this.longitude = longitude;
        this.latitude = latitude;
        this.exceptionTypeId = exceptionTypeId;
        this.instanceId = instanceId;
    }

    public BigInteger getInstanceId( ) {
        return instanceId;
    }

    public void setInstanceId( BigInteger instanceId ) {
        this.instanceId = instanceId;
    }

    public String getFromWho( ) {
        return fromWho;
    }

    public void setFromWho( String fromWho ) {
        this.fromWho = fromWho;
    }

    public String getToWho( ) {
        return toWho;
    }

    public void setToWho( String toWho ) {
        this.toWho = toWho;
    }

    public long getTimeInMillis( ) {
        return timeInMillis;
    }

    public void setTimeInMillis( long timeInMillis ) {
        this.timeInMillis = timeInMillis;
    }

    public double getLongitude( ) {
        return longitude;
    }

    public void setLongitude( double longitude ) {
        this.longitude = longitude;
    }

    public double getLatitude( ) {
        return latitude;
    }

    public void setLatitude( double latitude ) {
        this.latitude = latitude;
    }

    public int getExceptionTypeId( ) {
        return exceptionTypeId;
    }

    public void setExceptionTypeId( int exceptionTypeId ) {
        this.exceptionTypeId = exceptionTypeId;
    }
}
