package com.attilapalfi.exceptional.ui.question_views

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.persistence.ExceptionTypeStore
import com.attilapalfi.exceptional.rest.ExceptionRestConnector
import com.attilapalfi.exceptional.ui.helpers.ViewHelper
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-03.
 */
class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val imageView: ImageView
    private val exceptionName: TextView
    private val questionText: TextView
    private val noButton: Button
    private val yesButton: Button
    private val cityName: TextView
    private val dateText: TextView
    @Inject
    lateinit val viewHelper: ViewHelper
    @Inject
    lateinit val exceptionTypeStore: ExceptionTypeStore
    @Inject
    lateinit val exceptionRestConnector: ExceptionRestConnector

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
        imageView = itemView.findViewById(R.id.question_image) as ImageView
        exceptionName = itemView.findViewById(R.id.question_exception_name) as TextView
        questionText = itemView.findViewById(R.id.notif_question_text) as TextView
        noButton = itemView.findViewById(R.id.notif_question_no) as Button
        yesButton = itemView.findViewById(R.id.notif_question_yes) as Button
        cityName = itemView.findViewById(R.id.question_city_name) as TextView
        dateText = itemView.findViewById(R.id.question_date) as TextView
    }

    public fun bindRow(exception: Exception) {
        bindUserInfo(exception)
        bindExceptionInfo(exception)
        setClickListeners(exception)
    }

    private fun bindUserInfo(exception: Exception) {
        val fromWho = viewHelper.initExceptionSender(exception)
        val toWho = viewHelper.initExceptionReceiver(exception)
        viewHelper.bindExceptionImage(fromWho, toWho, imageView)
        cityName.text = viewHelper.getNameAndCity(exception, fromWho)
    }

    private fun bindExceptionInfo(exception: Exception) {
        if ( exception.exceptionType == null ) {
            exceptionName.text = exceptionTypeStore.findById(exception.exceptionTypeId).shortName
        } else {
            exceptionName.text = exception.shortName
        }
        questionText.text = exception.question.text
        dateText.text = viewHelper.formattedExceptionDate(exception)
    }

    private fun setClickListeners(exceptionQuestion: Exception) {
        noButton.isEnabled = true
        yesButton.isEnabled = true
        val listener = QuestionYesNoClickListener(exceptionQuestion, exceptionRestConnector, noButton, yesButton)
        noButton.setOnClickListener(listener)
        yesButton.setOnClickListener(listener)
    }
}
