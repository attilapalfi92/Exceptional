package com.attilapalfi.exceptional.services;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.inject.Inject;

import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.model.ExceptionType;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionTypeManager;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionInstanceWrapper;

/**
 * Created by palfi on 2015-08-30.
 */
public class ExceptionFactory {
    @Inject ExceptionTypeManager exceptionTypeManager;

    public ExceptionFactory( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );;
    }

    public Exception createFromWrapper( ExceptionInstanceWrapper wrapper ) {
        return new Exception( wrapper, exceptionTypeManager.findById( wrapper.getExceptionTypeId() ) );
    }

    public Exception createExceptionWithTypeId( int typeId, String fromWho, String toWho ) {
        ExceptionType exceptionType = exceptionTypeManager.findById( typeId );
        return createExceptionWithType( exceptionType, fromWho, toWho );
    }

    public Exception createExceptionWithType( ExceptionType type, String fromWho, String toWho ) {
        Exception exception = new Exception();
        exception.setFromWho( fromWho );
        exception.setToWho( toWho );
        exception.setDate( new Timestamp( Calendar.getInstance().getTimeInMillis() ) );
        exception.setExceptionType( type );
        return exception;
    }
}
