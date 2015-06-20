package com.attilapalf.exceptional.model;

import android.location.Location;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.Comparator;


/**
 * Created by Attila on 2015-06-08.
 */
public class Exception {
    ExceptionType exceptionType;

    // TODO: optimize exceptions: to be like on the server side

    // exception instance fields
    private long instanceId;
    private double longitude;
    private double latitude;
    private Calendar date;
    private long fromWho;
    private long toWho;

    private static Gson gson = new Gson();

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
    }

    public String getFullName() {
        return exceptionType.getPrefix() + exceptionType.getShortName();
    }

    public long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    public String getDescription() {
        return exceptionType.getDescription();
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
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

    public String getPrefix() {
        return exceptionType.getPrefix();
    }

    public String getShortName() {
        return exceptionType.getShortName();
    }

    public int getExceptionId() {
        return exceptionType.getTypeId();
    }
}
