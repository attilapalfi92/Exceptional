package com.attilapalfi.exceptional.services.rest.messages;

import com.attilapalfi.exceptional.model.Exception;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 212461305 on 2015.07.04..
 */
public class BaseExceptionRequest extends BaseRequest {
    protected List<BigInteger> knownExceptionIds;

    public BaseExceptionRequest() {}

    public BaseExceptionRequest(BigInteger userId, List<Exception> exceptionList) {
        this.userFacebookId = userId;

        knownExceptionIds = new ArrayList<>(exceptionList.size());
        for (Exception e : exceptionList) {
            knownExceptionIds.add(e.getInstanceId());
        }
    }

    public List<BigInteger> getKnownExceptionIds() {
        return knownExceptionIds;
    }

    public void setKnownExceptionIds(List<BigInteger> knownExceptionIds) {
        this.knownExceptionIds = knownExceptionIds;
    }
}
