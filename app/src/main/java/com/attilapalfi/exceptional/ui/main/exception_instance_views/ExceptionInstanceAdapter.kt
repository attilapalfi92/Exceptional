package com.attilapalfi.exceptional.ui.main.exception_instance_views

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Exception
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-03.
 */
class ExceptionInstanceAdapter(private var values: List<Exception>,
                               private val recyclerView: RecyclerView) : RecyclerView.Adapter<ExceptionInstanceViewHolder>() {
    @Inject
    lateinit var context: Context
    private var lastPosition = -1

    private val onClickListener = object : View.OnClickListener {
        override fun onClick(view: View) {
            val itemPosition = recyclerView.getChildLayoutPosition(view)
            //val exception = values[itemPosition]
            //Toast.makeText(context, exception.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExceptionInstanceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.exception_row_layout, parent, false)
        view.setOnClickListener(onClickListener)
        return ExceptionInstanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExceptionInstanceViewHolder, position: Int) {
        holder.bindRow(values[position])
        //setAnimation(holder.container, position)
    }

    override fun getItemCount() = values.size

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    fun setValues(values: List<Exception>) {
        this.values = values
    }
}
