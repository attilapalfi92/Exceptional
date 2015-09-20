package com.attilapalfi.exceptional.services;

/**
 * Created by palfi on 2015-09-20.
 */
public class LocationException extends Exception {
    public LocationException( ) {
    }

    public LocationException( String detailMessage ) {
        super( detailMessage );
    }

    public LocationException( String detailMessage, Throwable throwable ) {
        super( detailMessage, throwable );
    }

    public LocationException( Throwable throwable ) {
        super( throwable );
    }
}
