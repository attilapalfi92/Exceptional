package com.attilapalfi.exceptional.ui.main.exception_instance_views

import android.content.Context
import android.support.annotation.ColorInt
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.model.Friend
import com.attilapalfi.exceptional.persistence.MetadataStore
import com.attilapalfi.exceptional.ui.helpers.ViewHelper
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-03.
 */
public class ExceptionInstanceViewHolder(rowView: View) : RecyclerView.ViewHolder(rowView) {
    @Inject lateinit val context: Context
    @Inject lateinit val metadataStore: MetadataStore
    @Inject lateinit val viewHelper: ViewHelper
    private val friendImage: ImageView = rowView.findViewById(R.id.exc_row_image) as ImageView
    private val exceptionNameView: TextView = rowView.findViewById(R.id.question_exception_name) as TextView
    private val descriptionView: TextView = rowView.findViewById(R.id.exc_row_description) as TextView
    private val friendNameAndCityView: TextView = rowView.findViewById(R.id.exc_row_city_and_friend) as TextView
    private val toNameView: TextView = rowView.findViewById(R.id.exc_row_to_person) as TextView
    private val dateView: TextView = rowView.findViewById(R.id.exc_row_date) as TextView
    private val outgoingImage: ImageView = rowView.findViewById(R.id.exc_row_outgoing_image) as ImageView
    private val incomingImage: ImageView = rowView.findViewById(R.id.exc_row_incoming_image) as ImageView
    private val pointsForYouView: TextView = rowView.findViewById(R.id.exc_row_points_for_you) as TextView
    private val pointsForFriendView: TextView = rowView.findViewById(R.id.exc_row_points_for_friend) as TextView
    private lateinit var fromWho: Friend
    private lateinit var toWho: Friend
    private lateinit var user: Friend

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
    }

    public fun bindRow(model: com.attilapalfi.exceptional.model.Exception) {
        initBinding(model)
        bindUserInfo(model)
        bindExceptionInfo(model)
        setDirectionImages()
    }

    private fun initBinding(model: Exception) {
        fromWho = viewHelper.initExceptionSender(model)
        toWho = viewHelper.initExceptionReceiver(model)
        user = metadataStore.user
    }

    private fun bindUserInfo(model: Exception) {
        toNameView.text = toWho.getName()
        viewHelper.bindExceptionImage(fromWho, toWho, friendImage)
        setFromWhoNameAndCity(model)
        bindPointInfo(model)
    }

    private fun bindPointInfo(model: Exception) {
        if ( fromWho == user ) {
            setPointColor(R.color.exceptional_blue)
            pointsForYouView.text = model.pointsForSender.toString()
            pointsForFriendView.text = model.pointsForReceiver.toString()
        } else {
            setPointColor(R.color.exceptional_red)
            pointsForYouView.text = model.pointsForReceiver.toString()
            pointsForFriendView.text = model.pointsForSender.toString()
        }
    }

    // context.resources.getColor(color)
    private fun setPointColor(@ColorInt color: Int) {
        pointsForYouView.setTextColor(context.resources.getColor(color))
        pointsForFriendView.setTextColor(context.resources.getColor(color))
    }

    private fun setFromWhoNameAndCity(model: Exception) {
        val nameAndCity = viewHelper.getNameAndCity(model, fromWho)
        friendNameAndCityView.text = nameAndCity
    }

    private fun bindExceptionInfo(model: Exception) {
        exceptionNameView.text = model.shortName
        descriptionView.text = model.description
        dateView.text = viewHelper.formattedExceptionDate(model)
    }

    private fun setDirectionImages() {
        if (fromWho != user) {
            outgoingImage.setImageBitmap(null)
        } else {
            outgoingImage.setImageDrawable(context.resources.getDrawable(R.drawable.outgoing))
        }
        if (toWho != user) {
            incomingImage.setImageBitmap(null)
        } else {
            incomingImage.setImageDrawable(context.resources.getDrawable(R.drawable.incoming))
        }
    }
}
