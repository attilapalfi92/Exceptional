package com.attilapalfi.exceptional.rest.messages

import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.model.ExceptionType
import java.math.BigInteger

/**
 * Created by palfi on 2015-09-14.
 */
public data class AppStartRequest(var userFacebookId: BigInteger = BigInteger("0"),
                                  var knownExceptionIds: List<BigInteger> = listOf(),
                                  var deviceId: String = "",
                                  var gcmId: String = "",
                                  var friendsFacebookIds: Collection<BigInteger> = listOf(),
                                  var exceptionVersion: Int = 0,
                                  var firstName: String = "",
                                  var lastName: String = "",
                                  var deviceName: String = "");

public data class AppStartResponse(var myExceptions: List<ExceptionInstanceWrapper>,
                                   var exceptionTypes: List<ExceptionType>,
                                   var beingVotedTypes: List<ExceptionType>,
                                   var friendsPoints: Map<BigInteger, Int>,
                                   var points: Int,
                                   var exceptionVersion: Int,
                                   var submittedThisWeek: Boolean,
                                   var votedThisWeek: Boolean);

public data class BaseExceptionRequest(var userFacebookId: BigInteger, var knownExceptionIds: List<BigInteger>);

public data class ExceptionInstanceWrapper(var fromWho: BigInteger = BigInteger("0"),
                                           var toWho: BigInteger = BigInteger("0"),
                                           var timeInMillis: Long = 0,
                                           var longitude: Double = 0.0,
                                           var latitude: Double = 0.0,
                                           var exceptionTypeId: Int = 0,
                                           var instanceId: BigInteger = BigInteger("0")) {
    constructor(e: Exception) : this(
            e.getFromWho(),
            e.getToWho(),
            e.getDate().getTime(),
            e.getLongitude(),
            e.getLatitude(),
            e.getExceptionTypeId(),
            e.getInstanceId()
    )
};

public data class ExceptionRefreshResponse(var exceptionList: List<ExceptionInstanceWrapper>);

public data class ExceptionSentResponse(var exceptionShortName: String, var yourPoints: Int, var friendsPoints: Int,
                                        var instanceWrapper: ExceptionInstanceWrapper);

public data class SubmitRequest(var submitterId: BigInteger = BigInteger("0"), var submittedType: ExceptionType = ExceptionType());

public data class SubmitResponse(var submittedType: ExceptionType = ExceptionType(), var submittedThisWeek: Boolean = false);

public data class VoteRequest(var userId: BigInteger = BigInteger("0"), var votedExceptionId: Int = 0);

public data class VoteResponse(var votedForThisWeek: Boolean = false, var votedType: ExceptionType = ExceptionType());