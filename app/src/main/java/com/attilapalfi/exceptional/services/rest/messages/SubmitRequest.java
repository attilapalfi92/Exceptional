package com.attilapalfi.exceptional.services.rest.messages;

import java.math.BigInteger;

import com.attilapalfi.exceptional.model.ExceptionType;

/**
 * Created by palfi on 2015-09-06.
 */
public class SubmitRequest {
    private String submitterId;
    private ExceptionType submittedType;

    public SubmitRequest( ) {
    }

    public SubmitRequest( String submitterId, ExceptionType submittedType ) {
        this.submitterId = submitterId;
        this.submittedType = submittedType;
    }

    public String getSubmitterId( ) {
        return submitterId;
    }

    public void setSubmitterId( String submitterId ) {
        this.submitterId = submitterId;
    }

    public ExceptionType getSubmittedType( ) {
        return submittedType;
    }

    public void setSubmittedType( ExceptionType submittedType ) {
        this.submittedType = submittedType;
    }
}
