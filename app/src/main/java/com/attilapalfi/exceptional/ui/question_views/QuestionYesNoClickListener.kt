package com.attilapalfi.exceptional.ui.question_views

import android.view.View
import android.widget.Button
import com.attilapalf.exceptional.messages.QuestionAnswer
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.rest.ExceptionRestConnector

/**
 * Created by palfi on 2015-10-04.
 */
class QuestionYesNoClickListener(private val exception: Exception,
                                 private val exceptionRestConnector: ExceptionRestConnector,
                                 private val noButton: Button,
                                 private val yesButton: Button) : View.OnClickListener {

    override fun onClick(view: View) {
        disableButtons()
        val answer = questionAnswer(view)
        exceptionRestConnector.answerExceptionQuestion(answer)
    }

    private fun disableButtons() {
        noButton.isEnabled = false
        yesButton.isEnabled = false
    }

    private fun questionAnswer(view: View): QuestionAnswer {
        val answer = QuestionAnswer(exception.instanceId)
        if (view.id == R.id.notif_question_yes) {
            answer.answeredYes = true
        } else {
            answer.answeredYes = false
        }
        return answer
    }
}