package com.attilapalfi.exceptional.ui.question_views

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.model.QuestionException

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

    init {
        imageView = itemView.findViewById(R.id.question_image) as ImageView
        exceptionName = itemView.findViewById(R.id.question_exception_name) as TextView
        questionText = itemView.findViewById(R.id.question_text) as TextView
        noButton = itemView.findViewById(R.id.question_no) as Button
        yesButton = itemView.findViewById(R.id.question_yes) as Button
        cityName = itemView.findViewById(R.id.question_city_name) as TextView
        dateText = itemView.findViewById(R.id.question_date) as TextView
    }

    public fun bindRow( questionException: QuestionException ) {

    }
}