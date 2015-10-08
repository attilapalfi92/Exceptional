package com.attilapalfi.exceptional.ui.question_views

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.model.Exception

/**
 * Created by palfi on 2015-10-03.
 */
class QuestionAdapter(public var values: List<Exception>) : RecyclerView.Adapter<QuestionViewHolder>() {

    override fun getItemCount() = values.size()

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bindRow(values.get(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.question_row_layout, parent, false)
        return QuestionViewHolder(view)
    }
}