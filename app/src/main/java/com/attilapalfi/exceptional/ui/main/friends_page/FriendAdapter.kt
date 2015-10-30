package com.attilapalfi.exceptional.ui.main.friends_page

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.model.Friend
import com.attilapalfi.exceptional.persistence.ImageCache
import com.attilapalfi.exceptional.ui.main.Constants
import java.lang.ref.WeakReference

/**
 * Created by palfi on 2015-10-30.
 */
class FriendAdapter(private var values: List<Friend>,
                    private val activity: WeakReference<Activity>,
                    private val imageCache: ImageCache) : RecyclerView.Adapter<FriendViewHolder>() {

    private var recyclerView: RecyclerView? = null
    private var lastPosition = -1

    private val onClickListener = object : View.OnClickListener {
        override fun onClick(view: View) {
            val itemPosition = recyclerView!!.getChildLayoutPosition(view)
            val friend = values[itemPosition]
            val intent = Intent(activity.get(), FriendDetailsActivity::class.java)
            intent.putExtra(Constants.FRIEND_ID, friend.id.toString())
            activity.get().startActivity(intent)
        }
    }

    fun setValues(values: List<Friend>) {
        this.values = values
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_row_layout, parent, false)
        view.setOnClickListener(onClickListener)
        return FriendViewHolder(view, imageCache)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bindRow(values[position])
        //setAnimation(holder.container, position)
    }

    override fun getItemCount(): Int {
        return values.size
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(activity.get(), android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    fun setRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }
}
