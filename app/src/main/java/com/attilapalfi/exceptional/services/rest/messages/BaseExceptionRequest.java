package com.attilapalfi.exceptional.services.rest.messages;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.annimon.stream.Collectors;
import com.attilapalfi.exceptional.model.Exception;

import static com.annimon.stream.Stream.of;

/**
 * Created by 212461305 on 2015.07.04..
 */
public class BaseExceptionRequest extends BaseRequest {
    protected List<BigInteger> knownExceptionIds;

    public BaseExceptionRequest( ) {
    }

    public BaseExceptionRequest( BigInteger userId, List<Exception> exceptionList ) {
        this.userFacebookId = userId;

        knownExceptionIds = new ArrayList<>( exceptionList.size() );
        knownExceptionIds.addAll( of( exceptionList ).map( Exception::getInstanceId ).collect( Collectors.toList() ) );
    }

    public List<BigInteger> getKnownExceptionIds( ) {
        return knownExceptionIds;
    }

    public void setKnownExceptionIds( List<BigInteger> knownExceptionIds ) {
        this.knownExceptionIds = knownExceptionIds;
    }
}
