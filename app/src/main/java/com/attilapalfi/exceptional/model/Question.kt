package com.attilapalfi.exceptional.model

/**
 * Created by palfi on 2015-10-02.
 */
public data class Question(
        var text: String = "",
        var yesIsCorrect: Boolean = true,
        var hasQuestion: Boolean = false
)