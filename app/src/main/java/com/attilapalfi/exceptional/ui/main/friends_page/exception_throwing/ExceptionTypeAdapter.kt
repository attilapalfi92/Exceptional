package com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.model.ExceptionType
import java.lang.ref.WeakReference

/**
 * Created by palfi on 2015-10-02.
 */
public class ExceptionTypeAdapter(private val activity: WeakReference<Activity>,
                                  recyclerView: RecyclerView,
                                  private val values: List<ExceptionType>) : RecyclerView.Adapter<ExceptionTypeViewHolder>() {

    private val onClickListener: View.OnClickListener
    private var lastPosition = -1

    init {
        this.onClickListener = ExceptionTypeClickListener(values, recyclerView, activity.get())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExceptionTypeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.exception_type_row_layout, parent, false)
        view.setOnClickListener(onClickListener)
        return ExceptionTypeViewHolder(view, activity.get().applicationContext)
    }

    override fun onBindViewHolder(holder: ExceptionTypeViewHolder, position: Int) {
        holder.bindRow(values[position])
        //setAnimation(holder.container, position)
    }

    override fun getItemCount() = values.size

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(activity.get(), android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }
}
