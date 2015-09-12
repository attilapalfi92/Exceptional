package com.attilapalfi.exceptional.services.rest.messages;

import java.math.BigInteger;

import com.attilapalfi.exceptional.model.ExceptionType;

/**
 * Created by palfi on 2015-09-06.
 */
public class SubmitRequest {
    private BigInteger submitterId;
    private ExceptionType submittedType;

    public SubmitRequest( ) {
    }

    public SubmitRequest( BigInteger submitterId, ExceptionType submittedType ) {
        this.submitterId = submitterId;
        this.submittedType = submittedType;
    }

    public BigInteger getSubmitterId( ) {
        return submitterId;
    }

    public void setSubmitterId( BigInteger submitterId ) {
        this.submitterId = submitterId;
    }

    public ExceptionType getSubmittedType( ) {
        return submittedType;
    }

    public void setSubmittedType( ExceptionType submittedType ) {
        this.submittedType = submittedType;
    }
}
