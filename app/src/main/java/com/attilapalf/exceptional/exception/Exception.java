package com.attilapalf.exceptional.exception;

import android.location.Location;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.Comparator;


/**
 * Created by Attila on 2015-06-08.
 */
public class Exception {
    private int id;
    private String name;
    private String description;
    private Location location;
    private Calendar date;
    private String fromWho;
    private String toWho;

    private static Gson gson = new Gson();


    public static class NameComparator implements Comparator<Exception> {

        @Override
        public int compare(Exception lhs, Exception rhs) {
            return lhs.getName().compareTo(rhs.getName());
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

    public static Exception fromString(String json) {
        return gson.fromJson(json, Exception.class);
    }

    public Exception() {
        id = (int)(Math.random() * Integer.MAX_VALUE);
        name = "java." + id + ".pulyka." + "\nRandomException";
        description = name + " is thrown, when it's thrown. You just get it randomly, OK? " +
                "You better accept it, there is nothing to do.";
        fromWho = "Device";
        toWho = "You";

        date = Calendar.getInstance();
    }

    public static Exception getRandomException() {
        return new Exception();
    }

    public synchronized Location getLocation() {
        return location;
    }

    public synchronized void setLocation(Location location) {
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getFromWho() {
        return fromWho;
    }

    public void setFromWho(String fromWho) {
        this.fromWho = fromWho;
    }

    public String getToWho() {
        return toWho;
    }

    public void setToWho(String toWho) {
        this.toWho = toWho;
    }
}
