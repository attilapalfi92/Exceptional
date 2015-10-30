package com.attilapalfi.exceptional.ui.main.exception_instance_views

import android.content.Context
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
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
    @Inject lateinit var context: Context
    @Inject lateinit var metadataStore: MetadataStore
    @Inject lateinit var viewHelper: ViewHelper
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
    private val questionTextView: TextView = rowView.findViewById(R.id.exc_row_question_text) as TextView
    private val questionAnswerView: TextView = rowView.findViewById(R.id.exc_row_question_answer) as TextView
    public val container: CardView = rowView.findViewById(R.id.instance_row_container) as CardView
    private lateinit var fromWho: Friend
    private lateinit var toWho: Friend
    private lateinit var user: Friend

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
    }

    public fun bindRow(model: Exception) {
        initBinding(model)
        bindUserInfo(model)
        bindExceptionInfo(model)
        bindQuestionInfo(model)
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

    private fun setPointColor(@ColorInt color: Int) {
        pointsForYouView.setTextColor(ContextCompat.getColor(context, color))
        pointsForFriendView.setTextColor(ContextCompat.getColor(context, color))
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

    private fun bindQuestionInfo(model: Exception) {
        if (model.question.hasQuestion) {
            questionTextView.text = model.question.text
            if (model.question.isAnswered) {
                if (metadataStore.isItUser(model.sender)) {
                    if (model.question.answeredCorrectly) {
                        questionAnswerView.setTextColor(ContextCompat.getColor(context, R.color.exceptional_red))
                        questionAnswerView.text = context.getString(R.string.friend_aswered_correctly)
                    } else {
                        questionAnswerView.setTextColor(ContextCompat.getColor(context, R.color.exceptional_blue))
                        questionAnswerView.text = context.getString(R.string.friend_aswered_wrong)
                    }
                } else {
                    if (model.question.answeredCorrectly) {
                        questionAnswerView.setTextColor(ContextCompat.getColor(context, R.color.exceptional_blue))
                        questionAnswerView.text = context.getString(R.string.you_aswered_correctly)
                    } else {
                        questionAnswerView.setTextColor(ContextCompat.getColor(context, R.color.exceptional_red))
                        questionAnswerView.text = context.getString(R.string.you_aswered_wrong)
                    }
                }
            } else {
                if (metadataStore.isItUser(model.sender)) {
                    questionAnswerView.setTextColor(ContextCompat.getColor(context, R.color.grey))
                    questionAnswerView.text = context.getString(R.string.friend_still_has_to_answer)
                } else {
                    questionAnswerView.setTextColor(ContextCompat.getColor(context, R.color.black))
                    questionAnswerView.text = context.getString(R.string.you_still_have_to_answer)
                }
            }
        } else {
            questionTextView.visibility = View.GONE
            questionAnswerView.visibility = View.GONE
        }
    }

    private fun setDirectionImages() {
        if (fromWho != user) {
            outgoingImage.setImageBitmap(null)
        } else {
            outgoingImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.outgoing))
        }
        if (toWho != user) {
            incomingImage.setImageBitmap(null)
        } else {
            incomingImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.incoming))
        }
    }
}
