package com.attilapalfi.exceptional.services.gcm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.model.Question
import com.attilapalfi.exceptional.persistence.*
import com.attilapalfi.exceptional.rest.messages.ExceptionInstanceWrapper
import com.attilapalfi.exceptional.ui.ShowNotificationActivity
import com.attilapalfi.exceptional.ui.main.MainActivity
import com.google.android.gms.gcm.GcmListenerService
import java.math.BigInteger
import java.sql.Timestamp
import java.util.*
import javax.inject.Inject


/**
 * Created by 212461305 on 2015.06.29..
 */
public class ExceptionalGcmService : GcmListenerService() {   // IntentService("GcmMessageHandler") {
    private var handler: Handler? = null
    private var exception: Exception = Exception()
    private var geocoder: Geocoder? = null
    @Inject
    lateinit var exceptionInstanceStore: ExceptionInstanceStore
    @Inject
    lateinit var exceptionTypeStore: ExceptionTypeStore
    @Inject
    lateinit var friendStore: FriendStore
    @Inject
    lateinit var metadataStore: MetadataStore
    @Inject
    lateinit var questionStore: QuestionStore

    override fun onCreate() {
        super.onCreate()
        Injector.INSTANCE.applicationComponent.inject(this)
        geocoder = Geocoder(applicationContext, Locale.getDefault())
        handler = Handler(Looper.getMainLooper())
    }

    override fun onMessageReceived(from: String, data: Bundle) {
        val notificationType = data.getString("notificationType") ?: return
        handleNotification(data, notificationType)
    }

    private fun handleNotification(extras: Bundle, notificationType: String) {
        when (notificationType) {
            "exception" -> handleExceptionNotification(extras)
            "friend" -> handleFriendNotification(extras)
            "answer" -> handleAnswerNotification(extras)
            else -> {
            }
        }
    }

    private fun handleFriendNotification(extras: Bundle) {
        val fullName = extras.getString("fullName")
        showFriendNotification(fullName + " joined!", "Throw an exception into them face!")
    }

    private fun handleExceptionNotification(extras: Bundle) {
        parseNotificationToException(extras)
        saveExceptionAndPoints(extras)
        val bundle = createBundle()
        showExceptionNotification("New exception caught!", "You have caught a(n) " + exception.shortName, bundle)
    }

    private fun handleAnswerNotification(extras: Bundle) {
        val usersPoints = extras.getString("usersPoints").toInt()
        val friendsPoints = extras.getString("friendsPoints").toInt()
        val instanceId = BigInteger(extras.getString("instanceId"))
        exceptionInstanceStore.setAnswered(ExceptionInstanceWrapper(pointsForSender = usersPoints,
                pointsForReceiver = friendsPoints, instanceId = instanceId), true)

    }

    private fun parseNotificationToException(extras: Bundle) {
        exception = Exception()
        val typeId = Integer.parseInt(extras.getString("typeId"))
        exception.exceptionType = exceptionTypeStore.findById(typeId)
        exception.instanceId = BigInteger(extras.getString("instanceId"))
        exception.fromWho = BigInteger(extras.getString("fromWho"))
        exception.toWho = BigInteger(extras.getString("toWho"))
        exception.longitude = extras.getString("longitude")!!.toDouble()
        exception.latitude = extras.getString("latitude")!!.toDouble()
        exception.date = Timestamp(extras.getString("timeInMillis")!!.toLong())
        exception.pointsForSender = extras.getString("exceptionPointsForSender")!!.toInt()
        exception.pointsForReceiver = extras.getString("exceptionPointsForReceiver")!!.toInt()
        parseAndSaveQuestion(extras, exception)
    }

    private fun parseAndSaveQuestion(extras: Bundle, exception: Exception) {
        if ( extras.getString("hasQuestion").toBoolean() ) {
            val questionText = extras.getString("questionText")
            val yesIsCorrect = extras.getString("yesIsCorrect").toBoolean()
            exception.question = Question(questionText, yesIsCorrect, true, false)
            questionStore.addQuestion(exception)
        }
    }

    private fun createBundle(): Bundle {
        val bundle = Bundle()
        bundle.putString("instanceId", exception.instanceId.toString())
        return bundle
    }

    private fun saveExceptionAndPoints(extras: Bundle) {
        setCityForException(exception)
        exceptionInstanceStore.addExceptionWithoutCity(exception)
        handler?.post {
            savePoints(extras)
        }
    }

    private fun setCityForException(e: Exception) {
        try {
            e.city = geocoder?.getFromLocation(e.latitude, e.longitude, 1)?.get(0)?.locality ?: "Unknown"
        } catch (exception: java.lang.Exception) {
            e.city = getString(R.string.unknown)
            exception.printStackTrace()
        }
    }

    private fun savePoints(extras: Bundle) {
        val yourPointsString = extras.getString("yourPoints")
        if (yourPointsString != null) {
            metadataStore.setPoints(Integer.parseInt(yourPointsString))
        }
        val friendPointsString = extras.getString("friendPoints")
        if (friendPointsString != null) {
            friendStore.updateFriendPoints(exception.fromWho, Integer.parseInt(friendPointsString))
        }

    }

    private fun showFriendNotification(title: String, text: String) {
        val bundle = Bundle()
        bundle.putInt("startPage", 1)
        val resultPendingIntent = createPendingIntentForFriendNotification(bundle)
        val notification = buildNotification(title, text, resultPendingIntent)
        notifyUser(notification)
    }

    private fun createPendingIntentForFriendNotification(bundle: Bundle): PendingIntent {
        val stackBuilder = TaskStackBuilder.create(this)
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.putExtras(bundle)
        stackBuilder.addParentStack(MainActivity::class.java) // Adds the back stack
        stackBuilder.addNextIntent(resultIntent) // Adds the Intent to the top of the stack
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun buildNotification(title: String, text: String, resultPendingIntent: PendingIntent): Notification {
        return NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(resultPendingIntent)
                .build()
    }

    private fun notifyUser(notification: Notification) {
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(notificationIdCounter++, notification)
    }


    private fun showExceptionNotification(title: String, text: String, bundle: Bundle) {
        val pendingIntent = createPendingIntentForExceptionNotification(bundle)
        val notification = buildNotification(title, text, pendingIntent)
        notifyUser(notification)
    }

    private fun createPendingIntentForExceptionNotification(bundle: Bundle): PendingIntent {
        val resultIntent = Intent(this, ShowNotificationActivity::class.java)
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        resultIntent.putExtras(bundle) // putting data into the intent
        return PendingIntent.getActivity(// there's no need to create an artificial back stack.
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        private var notificationIdCounter = 0
    }
}
