package com.attilapalfi.exceptional.model

/**
 * Created by palfi on 2015-10-03.
 */
public data class QuestionException(
        public var question: Question = Question(),
        public var exception: Exception = Exception()) : Comparable<QuestionException> {

    override fun compareTo(other: QuestionException) =
            other.exception.instanceId.compareTo(exception.instanceId)
}


