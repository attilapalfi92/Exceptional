package com.attilapalfi.exceptional.ui.main.main_page.recycler_adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import com.attilapalfi.exceptional.ui.main.main_page.recycler_model.RowItemModel

/**
 * Created by palfi on 2015-10-26.
 */
public class MainViewHolder(val view: View?, val rowItemModel: RowItemModel) : RecyclerView.ViewHolder(view) {
    public fun bindRow() {
        view?.let {
            rowItemModel.bindRow(it)
        }
    }
}