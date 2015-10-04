package com.attilapalfi.exceptional.ui.question_views

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.ExceptionQuestion
import com.attilapalfi.exceptional.persistence.ExceptionTypeStore
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

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
        imageView = itemView.findViewById(R.id.question_image) as ImageView
        exceptionName = itemView.findViewById(R.id.question_exception_name) as TextView
        questionText = itemView.findViewById(R.id.question_text) as TextView
        noButton = itemView.findViewById(R.id.question_no) as Button
        yesButton = itemView.findViewById(R.id.question_yes) as Button
        cityName = itemView.findViewById(R.id.question_city_name) as TextView
        dateText = itemView.findViewById(R.id.question_date) as TextView
    }

    public fun bindRow(exceptionQuestion: ExceptionQuestion) {
        bindUserInfo(exceptionQuestion)
        bindExceptionInfo(exceptionQuestion)
        setClickListeners(exceptionQuestion)
    }

    private fun bindUserInfo(exceptionQuestion: ExceptionQuestion) {
        val fromWho = viewHelper.initExceptionSender(exceptionQuestion.exception)
        val toWho = viewHelper.initExceptionReceiver(exceptionQuestion.exception)
        viewHelper.bindExceptionImage(fromWho, toWho, imageView)
        cityName.text = viewHelper.getNameAndCity(exceptionQuestion.exception, fromWho)
    }

    private fun bindExceptionInfo(exceptionQuestion: ExceptionQuestion) {
        if ( exceptionQuestion.exception.exceptionType == null ) {
            exceptionName.text = exceptionTypeStore.findById(exceptionQuestion.exception.exceptionTypeId).shortName
        } else {
            exceptionName.text = exceptionQuestion.exception.shortName
        }
        questionText.text = exceptionQuestion.question.text
        dateText.text = viewHelper.formattedExceptionDate(exceptionQuestion.exception)
    }

    private fun setClickListeners(exceptionQuestion: ExceptionQuestion) {
        noButton.setOnClickListener({
            QuestionYesNoClickListener(exceptionQuestion)
        })
    }
}
