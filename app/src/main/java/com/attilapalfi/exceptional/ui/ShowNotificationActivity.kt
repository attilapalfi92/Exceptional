package com.attilapalfi.exceptional.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.model.ExceptionType
import com.attilapalfi.exceptional.model.Friend
import com.attilapalfi.exceptional.persistence.*
import com.attilapalfi.exceptional.rest.ExceptionRestConnector
import com.attilapalfi.exceptional.ui.helpers.ViewHelper
import com.attilapalfi.exceptional.ui.question_views.QuestionYesNoClickListener
import com.google.android.gms.maps.MapView
import java.math.BigInteger
import javax.inject.Inject


public class ShowNotificationActivity : AppCompatActivity(), QuestionChangeListener {
    @Inject
    lateinit val exceptionTypeStore: ExceptionTypeStore
    @Inject
    lateinit val friendStore: FriendStore
    @Inject
    lateinit val imageCache: ImageCache
    @Inject
    lateinit val metadataStore: MetadataStore
    @Inject
    lateinit val viewHelper: ViewHelper
    @Inject
    lateinit val exceptionInstanceStore: ExceptionInstanceStore
    @Inject
    lateinit val exceptionRestConnector: ExceptionRestConnector
    @Inject
    lateinit val questionStore: QuestionStore
    private lateinit var sender: Friend
    private lateinit var exception: Exception
    private lateinit var exceptionType: ExceptionType
    private lateinit var exceptionNameView: TextView
    private lateinit var exceptionDescView: TextView
    private lateinit var senderImageView: ImageView
    private lateinit var senderNameView: TextView
    private lateinit var sendDateView: TextView
    private lateinit var mapView: MapView
    private lateinit var questionText: TextView
    private lateinit var noButton: Button
    private lateinit var yesButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_notification)
        Injector.INSTANCE.applicationComponent.inject(this)
        questionStore.addChangeListener(this)
        getModelFromBundle()
        getViews()
        loadViewsWithData()
    }

    override fun onDestroy() {
        super.onDestroy()
        questionStore.removeChangeListener(this)
    }

    private fun getModelFromBundle() {
        val bundle = intent.extras
        val instanceId = BigInteger(bundle.getString("instanceId"))
        exception = exceptionInstanceStore.findById(instanceId)
        sender = friendStore.findById(exception.fromWho)
        exceptionType = exceptionTypeStore.findById(exception.exceptionTypeId)
    }

    private fun getViews() {
        exceptionNameView = findViewById(R.id.notif_full_exc_name) as TextView
        exceptionDescView = findViewById(R.id.notif_exc_desc) as TextView
        senderImageView = findViewById(R.id.notif_sender_image) as ImageView
        senderNameView = findViewById(R.id.notif_sender_name) as TextView
        sendDateView = findViewById(R.id.notif_sent_date) as TextView
        mapView = findViewById(R.id.notif_map) as MapView
        questionText = findViewById(R.id.notif_question_text) as TextView
        noButton = findViewById(R.id.notif_question_no) as Button
        yesButton = findViewById(R.id.notif_question_yes) as Button
    }

    private fun loadViewsWithData() {
        exceptionNameView.text = exceptionType.prefix + "\n" + exceptionType.shortName
        exceptionDescView.text = exceptionType.description
        imageCache.setImageToView(sender, senderImageView)
        senderNameView.text = viewHelper.getNameAndCity(exception, sender)
        sendDateView.text = exception.date.toString()
        loadQuestionToViews()
    }

    private fun loadQuestionToViews() {
        if (exception.question.hasQuestion) {
            showQuestionViews()
        } else {
            hideQuestionViews()
        }
    }

    private fun showQuestionViews() {
        questionText.text = exception.question.text
        val listener = QuestionYesNoClickListener(exception, exceptionRestConnector, noButton, yesButton)
        noButton.setOnClickListener(listener)
        yesButton.setOnClickListener(listener)
    }

    private fun hideQuestionViews() {
        questionText.visibility = View.INVISIBLE
        noButton.visibility = View.INVISIBLE
        yesButton.visibility = View.INVISIBLE
    }

    override fun onQuestionsChanged() {
        onBackPressed()
    }
}
