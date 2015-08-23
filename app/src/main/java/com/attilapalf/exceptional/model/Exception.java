package com.attilapalf.exceptional.model;

import com.attilapalf.exceptional.rest.messages.ExceptionInstanceWrapper;
import com.attilapalf.exceptional.services.persistent_stores.ExceptionTypeManager;
import com.google.gson.Gson;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Comparator;


/**
 * Created by Attila on 2015-06-08.
 */
public class Exception {
    private static Gson gson = new Gson();

    private transient ExceptionType exceptionType;
    private BigInteger instanceId;
    private int exceptionTypeId;
    private double longitude;
    private double latitude;
    private Timestamp date;
    private BigInteger fromWho;
    private BigInteger toWho;

    public Exception(ExceptionInstanceWrapper wrapper) {
        exceptionTypeId = wrapper.getExceptionTypeId();
        exceptionType = ExceptionTypeManager.getInstance().findById(exceptionTypeId);
        instanceId = wrapper.getInstanceId();
        longitude = wrapper.getLongitude();
        latitude = wrapper.getLatitude();
        date = new Timestamp(wrapper.getTimeInMillis());
        fromWho = wrapper.getFromWho();
        toWho = wrapper.getToWho();
    }

    public static class ShortNameComparator implements Comparator<Exception> {
        @Override
        public int compare(Exception lhs, Exception rhs) {
            return lhs.getShortName().compareTo(rhs.getShortName());
        }
    }


    public static class DateComparator implements Comparator<Exception> {
        @Override
        public int compare(Exception lhs, Exception rhs) {
            return rhs.getDate().compareTo(lhs.getDate());
        }
    }


    @Override
    public String toString() {
        return gson.toJson(this);
    }

    public Exception clone() {
        Exception e = new Exception();
        e.setExceptionType(exceptionType);

        return e;
    }

    public static Exception fromString(String json) {
        return gson.fromJson(json, Exception.class);
    }

    public Exception() {}

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

    public ExceptionType getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(ExceptionType exceptionType) {
        this.exceptionType = exceptionType;
        exceptionTypeId = exceptionType.getId();
    }

    public String getFullName() {
        return exceptionType.getPrefix() + exceptionType.getShortName();
    }

    public BigInteger getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(BigInteger instanceId) {
        this.instanceId = instanceId;
    }

    public String getDescription() {
        return exceptionType.getDescription();
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public BigInteger getFromWho() {
        return fromWho;
    }

    public void setFromWho(BigInteger fromWho) {
        this.fromWho = fromWho;
    }

    public BigInteger getToWho() {
        return toWho;
    }

    public void setToWho(BigInteger toWho) {
        this.toWho = toWho;
    }

    public String getPrefix() {
        return exceptionType.getPrefix();
    }

    public String getShortName() {
        return exceptionType.getShortName();
    }

    public int getExceptionTypeId() {
        return exceptionTypeId;
    }

    public void setExceptionTypeId(int exceptionTypeId) {
        this.exceptionTypeId = exceptionTypeId;
    }
}
