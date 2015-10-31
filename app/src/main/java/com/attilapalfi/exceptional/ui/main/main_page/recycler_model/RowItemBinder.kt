package com.attilapalfi.exceptional.ui.main.main_page.recycler_model

import android.view.View

/**
 * Created by palfi on 2015-10-26.
 */
public abstract class RowItemBinder {
    public abstract val rowType: RowType
    public abstract fun bindRow(rowView: View)
}