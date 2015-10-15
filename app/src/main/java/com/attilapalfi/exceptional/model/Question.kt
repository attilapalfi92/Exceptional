package com.attilapalfi.exceptional.model

/**
 * Created by palfi on 2015-10-02.
 */
public data class Question(
        val text: String? = null,
        val yesIsCorrect: Boolean = true,
        val hasQuestion: Boolean = false,
        var isAnswered: Boolean = true,
        var answeredCorrectly: Boolean = true
)