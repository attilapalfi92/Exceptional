package com.attilapalfi.exceptional.services.rest.messages

import com.attilapalfi.exceptional.model.ExceptionType
import java.math.BigInteger

/**
 * Created by palfi on 2015-09-14.
 */
data class AppStartRequest(var userFacebookId: BigInteger = BigInteger("0"),
                           var knownExceptionIds: List<BigInteger> = listOf(),
                           var deviceId: String = "",
                           var gcmId: String = "",
                           var friendsFacebookIds: Collection<BigInteger> = listOf(),
                           var exceptionVersion: Int = 0,
                           var firstName: String = "",
                           var lastName: String = "",
                           var deviceName: String = "");

data class AppStartResponse(var myExceptions: List<ExceptionInstanceWrapper>, var exceptionTypes: List<ExceptionType>,
                            var beingVotedTypes: List<ExceptionType>, var friendsPoints: Map<BigInteger, Int>,
                            var points: Int, var exceptionVersion: Int, var submittedThisWeek: Boolean,
                            var votedThisWeek: Boolean);

data class BaseExceptionRequest(var userFacebookId: BigInteger, var knownExceptionIds: List<BigInteger>);

data class ExceptionInstanceWrapper(var fromWho: BigInteger = BigInteger("0"),
                                    var toWho: BigInteger = BigInteger("0"),
                                    var timeInMillis: Long = 0,
                                    var longitude: Double = 0.0,
                                    var latitude: Double = 0.0,
                                    var exceptionTypeId: Int = 0,
                                    var instanceId: BigInteger = BigInteger("0")) {
    constructor(e: com.attilapalfi.exceptional.model.Exception) : this(
            e.getFromWho(),
            e.getToWho(),
            e.getDate().getTime(),
            e.getLongitude(),
            e.getLatitude(),
            e.getExceptionTypeId(),
            e.getInstanceId()
    )
};

data class ExceptionRefreshResponse(var exceptionList: List<ExceptionInstanceWrapper>);

data class ExceptionSentResponse(var exceptionShortName: String, var yourPoints: Int, var friendsPoints: Int,
                                 var instanceWrapper: ExceptionInstanceWrapper);

data class SubmitRequest(var submitterId: BigInteger = BigInteger("0"), var submittedType: ExceptionType = ExceptionType());

data class SubmitResponse(var submittedType: ExceptionType = ExceptionType(), var submittedThisWeek: Boolean = false);

data class VoteRequest(var userId: BigInteger = BigInteger("0"), var votedExceptionId: Int = 0);

data class VoteResponse(var votedForThisWeek: Boolean = false, var votedType: ExceptionType = ExceptionType());