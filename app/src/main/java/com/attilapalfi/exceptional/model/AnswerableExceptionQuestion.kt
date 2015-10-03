package com.attilapalfi.exceptional.model

/**
 * Created by palfi on 2015-10-03.
 */
public data class AnswerableExceptionQuestion(
        public var question: Question = Question(),
        public var fromWho: Friend = Friend(),
        public var exception: Exception = Exception()
)