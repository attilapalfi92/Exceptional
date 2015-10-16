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
    private val exceptionNameView: TextView by lazy { findViewById(R.id.notif_full_exc_name) as TextView }
    private val exceptionDescView: TextView by lazy { findViewById(R.id.notif_exc_desc) as TextView }
    private val senderImageView: ImageView by lazy { findViewById(R.id.notif_sender_image) as ImageView }
    private val senderNameView: TextView by lazy { findViewById(R.id.notif_sender_name) as TextView }
    private val sendDateView: TextView by lazy { findViewById(R.id.notif_sent_date) as TextView }
    private val mapView: MapView by lazy { findViewById(R.id.notif_map) as MapView }
    private val questionText: TextView by lazy { findViewById(R.id.notif_question_text) as TextView }
    private val noButton: Button by lazy { findViewById(R.id.notif_question_no) as Button }
    private val yesButton: Button by lazy { findViewById(R.id.notif_question_yes) as Button }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_notification)
        Injector.INSTANCE.applicationComponent.inject(this)
        questionStore.addChangeListener(this)
        getModelFromBundle()
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

    private fun loadViewsWithData() {
        exceptionNameView.text = exceptionType.prefix + "\n" + exceptionType.shortName
        exceptionDescView.text = exceptionType.description
        imageCache.setImageToView(sender, senderImageView)
        senderNameView.text = viewHelper.getNameAndCity(exception, sender)
        sendDateView.text = exception.date.toString()
        loadQuestionToViews()
    }

    private fun loadQuestionToViews() {
        if (exception.question.hasQuestion && !exception.question.isAnswered) {
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
