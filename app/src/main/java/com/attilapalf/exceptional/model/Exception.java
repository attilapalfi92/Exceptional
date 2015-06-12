package com.attilapalf.exceptional.model;

import android.location.Location;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.Comparator;


/**
 * Created by Attila on 2015-06-08.
 */
public class Exception {
    // meta exception fields
    private int exceptionId;
    private String shortName;
    private String prefix;
    private String description;

    // TODO: optimize exceptions: to be like on the server side

    // exception instance fields
    private String instanceId;
    private Location location;
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
        e.setExceptionId(exceptionId);
        e.setPrefix(prefix);
        e.setShortName(shortName);
        e.setDescription(description);

        return e;
    }

    public static Exception fromString(String json) {
        return gson.fromJson(json, Exception.class);
    }

    public Exception() {}

    public synchronized Location getLocation() {
        return location;
    }

    public synchronized void setLocation(Location location) {
        this.location = location;
    }

    public String getFullName() {
        return prefix + shortName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public int getExceptionId() {
        return exceptionId;
    }

    public void setExceptionId(int exceptionId) {
        this.exceptionId = exceptionId;
    }
}
