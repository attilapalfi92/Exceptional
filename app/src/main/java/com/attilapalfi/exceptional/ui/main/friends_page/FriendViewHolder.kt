package com.attilapalfi.exceptional.ui.main.friends_page

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.model.Friend
import com.attilapalfi.exceptional.persistence.ImageCache

/**
 * Created by palfi on 2015-10-30.
 */
class FriendViewHolder(rowView: View, private val imageCache: ImageCache) : RecyclerView.ViewHolder(rowView) {
    private val nameView: TextView
    private val pointsView: TextView
    private val imageView: ImageView
    public val container: CardView

    init {
        nameView = rowView.findViewById(R.id.friendNameView) as TextView
        pointsView = rowView.findViewById(R.id.friendPointsView) as TextView
        imageView = rowView.findViewById(R.id.friendImageView) as ImageView
        container = rowView.findViewById(R.id.friend_row_container) as CardView
    }

    fun bindRow(model: Friend) {
        nameView.text = model.firstName + " " + model.lastName
        pointsView.text = "Points: " + model.points
        imageCache.setImageToView(model, imageView)
    }
}