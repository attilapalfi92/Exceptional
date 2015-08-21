package com.attilapalf.exceptional.rest.messages;

import com.attilapalf.exceptional.model.Exception;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 212461305 on 2015.07.04..
 */
public class BaseExceptionRequestBody extends BaseRequestBody {
    protected List<BigInteger> knownExceptionIds;

    public BaseExceptionRequestBody() {}

    public BaseExceptionRequestBody(BigInteger userId, List<Exception> exceptionList) {
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
