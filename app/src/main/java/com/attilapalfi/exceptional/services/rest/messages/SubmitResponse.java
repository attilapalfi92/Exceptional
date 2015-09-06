package com.attilapalfi.exceptional.services.rest.messages;

import com.attilapalfi.exceptional.model.ExceptionType;

/**
 * Created by palfi on 2015-09-06.
 */
public class SubmitResponse {
    private ExceptionType submittedType;
    private boolean submittedThisWeek;

    public SubmitResponse() {
    }

    public SubmitResponse(ExceptionType submittedType, boolean submittedThisWeek) {
        this.submittedType = submittedType;
        this.submittedThisWeek = submittedThisWeek;
    }

    public ExceptionType getSubmittedType() {
        return submittedType;
    }

    public void setSubmittedType(ExceptionType submittedType) {
        this.submittedType = submittedType;
    }

    public boolean isSubmittedThisWeek() {
        return submittedThisWeek;
    }

    public void setSubmittedThisWeek(boolean submittedThisWeek) {
        this.submittedThisWeek = submittedThisWeek;
    }
}
