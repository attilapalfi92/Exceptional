package com.attilapalfi.exceptional.services;

import com.attilapalfi.exceptional.model.*;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionTypeManager;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Created by palfi on 2015-08-30.
 */
public class ExceptionFactory {

    public static Exception createExceptionWithTypeId(int typeId, BigInteger fromWho, BigInteger toWho) {
        ExceptionType exceptionType = ExceptionTypeManager.findById(typeId);
        return createExceptionWithType(exceptionType, fromWho, toWho);
    }

    public static Exception createExceptionWithType(ExceptionType type, BigInteger fromWho, BigInteger toWho) {
        Exception exception = new Exception();
        exception.setFromWho(fromWho);
        exception.setToWho(toWho);
        exception.setDate(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        exception.setExceptionType(type);
        return exception;
    }
}
