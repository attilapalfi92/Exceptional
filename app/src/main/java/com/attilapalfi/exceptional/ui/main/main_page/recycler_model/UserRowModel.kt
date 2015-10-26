package com.attilapalfi.exceptional.ui.main.main_page.recycler_model

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Friend
import com.attilapalfi.exceptional.persistence.ImageCache
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-26.
 */
public class UserRowModel(val user: Friend) : RowItemModel() {
    override val rowType: RowType = RowType.USER_ROW
    @Inject
    lateinit var imageCache: ImageCache
    @Inject
    lateinit var context: Context
    private var view: View? = null

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
    }

    override fun bindRow(rowView: View) {
        view = rowView
        setViews()
    }

    private fun setViews() {
        view?.let {
            setImageView(it)
            setNameView(it)
            setPointView(it)
        }
    }

    private fun setImageView(rowView: View) {
        val imageView = rowView.findViewById(R.id.myMainImageView) as ImageView
        imageCache.setImageToView(user, imageView)
    }

    private fun setNameView(rowView: View) {
        val nameView = rowView.findViewById(R.id.mainNameTextView) as TextView
        val nameText = "${context.getString(R.string.main_welcome_before_name)} " +
                "${user.firstName.trim { it <= ' ' }}!"
        nameView.text = nameText
    }

    private fun setPointView(rowView: View) {
        val pointView = rowView.findViewById(R.id.mainPointTextView) as TextView
        val pointText = "${context.getString(R.string.main_point_view_pre)} ${user.points} " +
                "${context.getString(R.string.main_point_view_post)}"
        pointView.text = pointText
    }
}