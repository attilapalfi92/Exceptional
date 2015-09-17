package com.attilapalfi.exceptional.model;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Comparator;

import com.attilapalfi.exceptional.services.rest.messages.ExceptionInstanceWrapper;
import com.google.gson.Gson;


/**
 * Created by Attila on 2015-06-08.
 */
public class Exception {
    private static Gson gson = new Gson();

    private transient ExceptionType exceptionType;
    private BigInteger instanceId = new BigInteger( "0" );
    private int exceptionTypeId;
    private double longitude;
    private double latitude;
    private Timestamp date; // TODO: long timestapm
    private BigInteger fromWho;
    private BigInteger toWho;
    private String city = "";

    public Exception( ExceptionInstanceWrapper wrapper, ExceptionType exceptionType ) {
        this.exceptionTypeId = wrapper.getExceptionTypeId();
        this.exceptionType = exceptionType;
        this.instanceId = wrapper.getInstanceId();
        this.longitude = wrapper.getLongitude();
        this.latitude = wrapper.getLatitude();
        this.date = new Timestamp( wrapper.getTimeInMillis() );
        this.fromWho = wrapper.getFromWho();
        this.toWho = wrapper.getToWho();
    }

    public static class ShortNameComparator implements Comparator<Exception> {
        @Override
        public int compare( Exception lhs, Exception rhs ) {
            return lhs.getShortName().compareTo( rhs.getShortName() );
        }
    }


    public static class DateComparator implements Comparator<Exception> {
        @Override
        public int compare( Exception lhs, Exception rhs ) {
            return rhs.getDate().compareTo( lhs.getDate() );
        }
    }


    @Override
    public String toString( ) {
        return gson.toJson( this );
    }

    public Exception clone( ) {
        Exception e = new Exception();
        e.setExceptionType( exceptionType );
        return e;
    }

    public static Exception fromString( String json ) {
        return gson.fromJson( json, Exception.class );
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

    public String getCity( ) {
        return city;
    }

    public void setCity( String city ) {
        this.city = city;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        Exception exception = (Exception) o;

        if ( instanceId != null ? !instanceId.equals( exception.instanceId ) : exception.instanceId != null )
            return false;
        return !( date != null ? !date.equals( exception.date ) : exception.date != null );
    }

    @Override
    public int hashCode( ) {
        int result = instanceId != null ? instanceId.hashCode() : 0;
        result = 31 * result + ( date != null ? date.hashCode() : 0 );
        return result;
    }
}
