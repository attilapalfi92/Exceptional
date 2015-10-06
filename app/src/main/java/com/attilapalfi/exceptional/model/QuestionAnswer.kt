package com.attilapalf.exceptional.messages

import java.math.BigInteger

/**
 * Created by palfi on 2015-10-06.
 */
public data class QuestionAnswer(var exceptionInstanceId: BigInteger = BigInteger.ZERO,
                                 var answeredYes: Boolean = true)