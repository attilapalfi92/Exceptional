package com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing

import javax.inject.Inject

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.ExceptionFactory
import com.attilapalfi.exceptional.model.ExceptionType
import com.attilapalfi.exceptional.persistence.FriendStore
import com.attilapalfi.exceptional.persistence.MetadataStore
import com.attilapalfi.exceptional.rest.ExceptionService
import com.attilapalfi.exceptional.services.LocationProvider

/**
 * Created by palfi on 2015-10-02.
 */
public class ExceptionTypeAdapter(private val activity: Activity,
                                  recyclerView: RecyclerView,
                                  private val values: List<ExceptionType>) : RecyclerView.Adapter<ExceptionTypeRowViewHolder>() {
    private val onClickListener: View.OnClickListener
    @Inject
    lateinit val locationProvider: LocationProvider
    @Inject
    lateinit val exceptionFactory: ExceptionFactory
    @Inject
    lateinit val exceptionService: ExceptionService
    @Inject
    lateinit val friendStore: FriendStore
    @Inject
    lateinit val metadataStore: MetadataStore

    init {
        this.onClickListener = ExceptionTypeClickListener(values, recyclerView, activity)
        Injector.INSTANCE.applicationComponent.inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExceptionTypeRowViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.exception_type_row_layout, parent, false)
        view.setOnClickListener(onClickListener)
        return ExceptionTypeRowViewHolder(view, activity.applicationContext)
    }

    override fun onBindViewHolder(holder: ExceptionTypeRowViewHolder, position: Int) {
        holder.bindRow(values.get(position))
    }

    override fun getItemCount(): Int {
        return values.size()
    }
}
