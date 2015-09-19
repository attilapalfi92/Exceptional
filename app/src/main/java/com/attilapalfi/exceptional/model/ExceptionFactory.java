package com.attilapalfi.exceptional.model;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.inject.Inject;

import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.persistence.ExceptionTypeStore;
import com.attilapalfi.exceptional.rest.messages.ExceptionInstanceWrapper;

/**
 * Created by palfi on 2015-08-30.
 */
public class ExceptionFactory {
    @Inject
    ExceptionTypeStore exceptionTypeStore;

    public ExceptionFactory( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );;
    }

    public Exception createFromWrapper( ExceptionInstanceWrapper wrapper ) {
        return new Exception( wrapper, exceptionTypeStore.findById( wrapper.getExceptionTypeId() ) );
    }

    public Exception createExceptionWithTypeId( int typeId, BigInteger fromWho, BigInteger toWho ) {
        ExceptionType exceptionType = exceptionTypeStore.findById( typeId );
        return createExceptionWithType( exceptionType, fromWho, toWho );
    }

    public Exception createExceptionWithType( ExceptionType type, BigInteger fromWho, BigInteger toWho ) {
        Exception exception = new Exception();
        exception.setFromWho( fromWho );
        exception.setToWho( toWho );
        exception.setDate( new Timestamp( Calendar.getInstance().getTimeInMillis() ) );
        exception.setExceptionType( type );
        return exception;
    }
}
