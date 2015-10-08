package com.attilapalfi.exceptional.model;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Comparator;

import org.jetbrains.annotations.NotNull;

import com.attilapalfi.exceptional.rest.messages.ExceptionInstanceWrapper;
import com.google.gson.Gson;


/**
 * Created by Attila on 2015-06-08.
 */
public class Exception implements Comparable<Exception> {
    private BigInteger instanceId = BigInteger.ZERO;
    private int exceptionTypeId = 0;
    private double longitude;
    private double latitude;
    private Timestamp date;
    private BigInteger fromWho;
    private BigInteger toWho;
    private Question question;
    @NotNull
    private volatile String city = "";
    private transient ExceptionType exceptionType;
    private transient Friend sender;
    private transient Friend receiver;

    public Exception( ExceptionInstanceWrapper wrapper, ExceptionType exceptionType ) {
        this.exceptionTypeId = exceptionType.getId();
        this.exceptionType = exceptionType;
        this.instanceId = wrapper.getInstanceId();
        this.longitude = wrapper.getLongitude();
        this.latitude = wrapper.getLatitude();
        this.date = new Timestamp( wrapper.getTimeInMillis() );
        this.fromWho = wrapper.getFromWho();
        this.toWho = wrapper.getToWho();
        this.question = wrapper.getQuestion();
    }

    @Override
    public int compareTo( Exception another ) {
        return another.instanceId.compareTo( instanceId );
    }

    public Exception clone( ) {
        Exception e = new Exception();
        e.setExceptionType( exceptionType );
        return e;
    }

    public Exception( ) {
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

    public ExceptionType getExceptionType( ) {
        return exceptionType;
    }

    public void setExceptionType( ExceptionType exceptionType ) {
        this.exceptionType = exceptionType;
        exceptionTypeId = exceptionType.getId();
    }

    public String getFullName( ) {
        return exceptionType.getPrefix() + exceptionType.getShortName();
    }

    public BigInteger getInstanceId( ) {
        return instanceId;
    }

    public void setInstanceId( BigInteger instanceId ) {
        this.instanceId = instanceId;
    }

    public String getDescription( ) {
        return exceptionType.getDescription();
    }

    public Timestamp getDate( ) {
        return date;
    }

    public void setDate( Timestamp date ) {
        this.date = date;
    }

    public BigInteger getFromWho( ) {
        return fromWho;
    }

    public void setFromWho( BigInteger fromWho ) {
        this.fromWho = fromWho;
    }

    public BigInteger getToWho( ) {
        return toWho;
    }

    public void setToWho( BigInteger toWho ) {
        this.toWho = toWho;
    }

    public String getPrefix( ) {
        return exceptionType.getPrefix();
    }

    public String getShortName( ) {
        return exceptionType.getShortName();
    }

    public int getExceptionTypeId( ) {
        return exceptionTypeId;
    }

    public void setExceptionTypeId( int exceptionTypeId ) {
        this.exceptionTypeId = exceptionTypeId;
    }

    public Friend getSender( ) {
        return sender;
    }

    public void setSender( Friend sender ) {
        this.sender = sender;
    }

    public Friend getReceiver( ) {
        return receiver;
    }

    public void setReceiver( Friend receiver ) {
        this.receiver = receiver;
    }

    public Question getQuestion( ) {
        return question;
    }

    public void setQuestion( Question question ) {
        this.question = question;
    }

    @NotNull
    public String getCity( ) {
        return city;
    }

    public void setCity( String city ) {
        if ( city != null ) {
            this.city = city;
        }
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Exception exception = (Exception) o;
        return !( instanceId != null ? !instanceId.equals( exception.instanceId ) : exception.instanceId != null );
    }

    @Override
    public int hashCode( ) {
        return instanceId != null ? instanceId.hashCode() : 0;
    }
}
