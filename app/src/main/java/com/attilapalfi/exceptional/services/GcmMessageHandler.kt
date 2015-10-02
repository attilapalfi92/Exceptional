package com.attilapalfi.exceptional.services

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v4.content.WakefulBroadcastReceiver
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.persistence.ExceptionInstanceStore
import com.attilapalfi.exceptional.persistence.ExceptionTypeStore
import com.attilapalfi.exceptional.persistence.FriendStore
import com.attilapalfi.exceptional.persistence.MetadataStore
import com.attilapalfi.exceptional.ui.ShowNotificationActivity
import com.attilapalfi.exceptional.ui.main.main_page.MainActivity
import java.math.BigInteger
import java.sql.Timestamp
import javax.inject.Inject


/**
 * Created by 212461305 on 2015.06.29..
 */
public class GcmMessageHandler : IntentService("GcmMessageHandler") {
    private var handler: Handler? = null
    private var exception: Exception? = null
    @Inject
    lateinit val exceptionInstanceStore: ExceptionInstanceStore
    @Inject
    lateinit val exceptionTypeStore: ExceptionTypeStore
    @Inject
    lateinit val friendStore: FriendStore
    @Inject
    lateinit val metadataStore: MetadataStore

    override fun onCreate() {
        super.onCreate()
        Injector.INSTANCE.applicationComponent.inject(this)
        handler = Handler(Looper.getMainLooper())
    }

    override fun onHandleIntent(intent: Intent) {
        try {
            val extras = intent.extras
            val notificationType = extras.getString("notificationType") ?: return
            handleNotification(extras, notificationType)
        } finally {
            WakefulBroadcastReceiver.completeWakefulIntent(intent)
        }
    }

    private fun handleNotification(extras: Bundle, notificationType: String) {
        when (notificationType) {
            "exception" -> handleExceptionNotification(extras)
            "friend" -> handleFriendNotification(extras)
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
        saveDataOnMainThread(extras)
        val bundle = createBundle()
        showExceptionNotification("New exception caught!", "You have caught a(n) " + exception!!.shortName, bundle)
    }

    private fun parseNotificationToException(extras: Bundle) {
        initException()
        val typeId = Integer.parseInt(extras.getString("typeId"))
        exception!!.exceptionType = exceptionTypeStore.findById(typeId)
        exception!!.instanceId = BigInteger(extras.getString("instanceId"))
        exception!!.fromWho = BigInteger(extras.getString("fromWho"))
        exception!!.toWho = BigInteger(extras.getString("toWho"))
        exception!!.longitude = java.lang.Double.parseDouble(extras.getString("longitude"))
        exception!!.latitude = java.lang.Double.parseDouble(extras.getString("latitude"))
        exception!!.date = Timestamp(java.lang.Long.parseLong(extras.getString("timeInMillis")))
    }

    private fun initException() {
        exception = Exception()
    }

    private fun createBundle(): Bundle {
        val bundle = Bundle()
        bundle.putInt("typeId", exception!!.exceptionTypeId)
        bundle.putString("fromWho", exception!!.fromWho.toString())
        bundle.putDouble("longitude", exception!!.longitude)
        bundle.putDouble("latitude", exception!!.latitude)
        bundle.putLong("timeInMillis", exception!!.date.time)
        return bundle
    }

    private fun saveDataOnMainThread(extras: Bundle) {
        handler!!.post {
            exceptionInstanceStore.addExceptionAsync(exception)
            savePoints(extras)
        }
    }

    private fun savePoints(extras: Bundle) {
        val yourPointsString = extras.getString("yourPoints")
        if (yourPointsString != null) {
            metadataStore.points = Integer.parseInt(yourPointsString)
        }
        val friendPointsString = extras.getString("friendPoints")
        if (friendPointsString != null) {
            friendStore.updateFriendPoints(exception!!.fromWho, Integer.parseInt(friendPointsString))
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
