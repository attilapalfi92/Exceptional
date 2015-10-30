package com.attilapalfi.exceptional.ui.main.main_page.recycler_adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.persistence.MetadataStore
import com.attilapalfi.exceptional.ui.main.main_page.recycler_model.*
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-26.
 */
public class MainAdapter(val values: List<RowItemModel>, val recyclerView: RecyclerView) : RecyclerView.Adapter<MainViewHolder>() {
    @Inject
    lateinit var metadataStore: MetadataStore

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
    }

    override fun getItemViewType(position: Int): Int {
        return values[position].rowType.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MainViewHolder? {
        when (viewType) {
            RowType.USER_ROW.ordinal -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.main_user_row_layout, parent, false)
                return MainViewHolder(view, UserRowModel(metadataStore.user))
            }
            RowType.FRIEND_POINTS_CHART.ordinal -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.main_friend_points_row_layout, parent, false)
                return MainViewHolder(view, FriendPointsChartModel())
            }
            RowType.TYPES_PIE_CHART.ordinal -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.main_type_throw_row_layout, parent, false)
                return MainViewHolder(view, TypeThrowChartModel())
            }
            else -> {
                return null
            }
        }
    }

    override fun onBindViewHolder(holder: MainViewHolder?, position: Int) {
        holder?.bindRow()
    }

    override fun getItemCount() = values.size
}