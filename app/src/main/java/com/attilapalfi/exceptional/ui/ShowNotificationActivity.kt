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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.math.BigInteger
import javax.inject.Inject


public class ShowNotificationActivity : AppCompatActivity(), QuestionChangeListener {
    @Inject
    lateinit var exceptionTypeStore: ExceptionTypeStore
    @Inject
    lateinit var friendStore: FriendStore
    @Inject
    lateinit var imageCache: ImageCache
    @Inject
    lateinit var metadataStore: MetadataStore
    @Inject
    lateinit var viewHelper: ViewHelper
    @Inject
    lateinit var exceptionInstanceStore: ExceptionInstanceStore
    @Inject
    lateinit var exceptionRestConnector: ExceptionRestConnector
    @Inject
    lateinit var questionStore: QuestionStore
    private lateinit var sender: Friend
    private lateinit var exception: Exception
    private lateinit var exceptionType: ExceptionType
    private val exceptionNameView: TextView by lazy { findViewById(R.id.notif_full_exc_name) as TextView }
    private val exceptionDescView: TextView by lazy { findViewById(R.id.notif_exc_desc) as TextView }
    private val senderImageView: ImageView by lazy { findViewById(R.id.notif_sender_image) as ImageView }
    private val senderNameView: TextView by lazy { findViewById(R.id.notif_sender_name) as TextView }
    private val sendDateView: TextView by lazy { findViewById(R.id.notif_sent_date) as TextView }
    private val questionText: TextView by lazy { findViewById(R.id.notif_question_text) as TextView }
    private val noButton: Button by lazy { findViewById(R.id.notif_question_no) as Button }
    private val yesButton: Button by lazy { findViewById(R.id.notif_question_yes) as Button }
    private val receivedMarker by lazy { BitmapDescriptorFactory.fromResource(R.drawable.incoming_big) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_notification)
        Injector.INSTANCE.applicationComponent.inject(this)
        questionStore.addChangeListener(this)
        getModelFromBundle()
        loadViewsWithData()
        loadMap()
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

    private fun loadMap() {
        val mapFragment = fragmentManager.findFragmentById(R.id.notif_map) as MapFragment
        mapFragment.getMapAsync {
            setMarker(it)
            setPosition(it)
        }
    }

    private fun setMarker(googleMap: GoogleMap) {
        val markerOptions = MarkerOptions()
                .position(LatLng(exception.latitude, exception.longitude))
                .draggable(false)
                .icon(receivedMarker)
        googleMap.addMarker(markerOptions)
    }

    private fun setPosition(googleMap: GoogleMap) {
        val cameraPosition = CameraPosition.Builder()
                .target(LatLng(exception.latitude, exception.longitude))
                .zoom(15f)
                .tilt(30f)
                .build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    override fun onQuestionsChanged() {
        onBackPressed()
    }
}
