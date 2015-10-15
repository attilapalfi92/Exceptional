package com.attilapalfi.exceptional.rest.messages

import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.model.ExceptionType
import com.attilapalfi.exceptional.model.Question
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
                                           var instanceId: BigInteger = BigInteger("0"),
                                           var pointsForSender: Int = 25,
                                           var pointsForReceiver: Int = -20,
                                           var question: Question = Question()) {
    constructor(e: Exception, question_: Question) : this(
            fromWho = e.fromWho,
            toWho = e.toWho,
            timeInMillis = e.date.time,
            longitude = e.longitude,
            latitude = e.latitude,
            exceptionTypeId = e.exceptionTypeId,
            instanceId = e.instanceId,
            pointsForSender = e.pointsForSender,
            pointsForReceiver = e.pointsForReceiver,
            question = question_
    )
};

public data class ExceptionRefreshResponse(var exceptionList: List<ExceptionInstanceWrapper>);

public data class ExceptionSentResponse(var exceptionShortName: String,
                                        var sendersPoints: Int,
                                        var receiversPoints: Int,
                                        var instanceWrapper: ExceptionInstanceWrapper);

public data class SubmitRequest(var submitterId: BigInteger = BigInteger("0"), var submittedType: ExceptionType = ExceptionType());

public data class SubmitResponse(var submittedType: ExceptionType = ExceptionType(), var submittedThisWeek: Boolean = false);

public data class VoteRequest(var userId: BigInteger = BigInteger("0"), var votedExceptionId: Int = 0);

public data class VoteResponse(var votedForThisWeek: Boolean = false, var votedType: ExceptionType = ExceptionType());