package com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.model.ExceptionType

/**
 * Created by palfi on 2015-10-02.
 */
public class ExceptionTypeAdapter(private val activity: Activity,
                                  recyclerView: RecyclerView,
                                  private val values: List<ExceptionType>) : RecyclerView.Adapter<ExceptionTypeViewHolder>() {

    private val onClickListener: View.OnClickListener

    init {
        this.onClickListener = ExceptionTypeClickListener(values, recyclerView, activity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExceptionTypeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.exception_type_row_layout, parent, false)
        view.setOnClickListener(onClickListener)
        return ExceptionTypeViewHolder(view, activity.applicationContext)
    }

    override fun onBindViewHolder(holder: ExceptionTypeViewHolder, position: Int) {
        holder.bindRow(values.get(position))
    }

    override fun getItemCount() = values.size()
}
