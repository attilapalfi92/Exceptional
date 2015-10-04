package com.attilapalfi.exceptional.model

/**
 * Created by palfi on 2015-10-03.
 */
public data class ExceptionQuestion(
        public var question: Question = Question(),
        public var exception: Exception = Exception()) : Comparable<ExceptionQuestion> {

    override fun compareTo(other: ExceptionQuestion) =
            other.exception.instanceId.compareTo(exception.instanceId)
}


