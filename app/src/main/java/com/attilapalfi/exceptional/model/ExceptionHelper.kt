package com.attilapalfi.exceptional.model

import android.content.Context
import android.location.Geocoder
import android.text.format.DateFormat
import android.widget.ImageView
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.persistence.FriendStore
import com.attilapalfi.exceptional.persistence.ImageCache
import com.attilapalfi.exceptional.persistence.MetadataStore
import java.math.BigInteger
import java.util.*
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-31.
 */
public class ExceptionHelper {
    @Inject
    lateinit var friendStore: FriendStore
    @Inject
    lateinit var metadataStore: MetadataStore
    @Inject
    lateinit var imageCache: ImageCache
    @Inject
    lateinit var context: Context
    private val geocoder: Geocoder

    companion object {
        private val UNKNOWN_FRIEND = Friend()
    }

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
        geocoder = Geocoder(context, Locale.getDefault())
    }

    public fun geoDecodeCity(exception: Exception) {
        try {
            exception.city = geocoder.getFromLocation(exception.latitude, exception.longitude, 1)[0].locality
        } catch (e: java.lang.Exception) {
            exception.city = context.getString(R.string.unknown)
            e.printStackTrace()
        }
    }

    public fun getNameAndCity(exception: Exception, fromWho: Friend): String {
        var senderName = fromWho.getName()
        return addCity(senderName, exception.city)
    }

    private fun addCity(senderName: String, city: String): String {
        var nameAndCity = senderName
        if ("" != city) {
            nameAndCity += (", " + city)
        }
        return nameAndCity
    }

    public fun initExceptionSender(exception: Exception): Friend {
        if (senderIsUnknown(exception)) {
            findSender(exception)
        }
        return exception.sender
    }

    private fun findSender(exception: Exception) {
        var fromWho = friendStore.findById(exception.fromWho)
        if (otherIsNotFriend(fromWho)) {
            fromWho = findNotFriendSender(exception)
        }
        exception.sender = fromWho
    }

    private fun findNotFriendSender(exception: Exception): Friend {
        if (exception.fromWho == metadataStore.user.id) {
            return metadataStore.user
        } else {
            return UNKNOWN_FRIEND
        }
    }

    private fun senderIsUnknown(exception: Exception) = exception.sender == null || exception.sender.id == BigInteger.ZERO

    public fun initExceptionReceiver(exception: Exception): Friend {
        if (receiverIsUnknown(exception)) {
            findReceiver(exception)
        }
        return exception.receiver
    }

    private fun findReceiver(exception: Exception) {
        var toWho = friendStore.findById(exception.toWho)
        if (otherIsNotFriend(toWho)) {
            toWho = findNotFriendReceiver(exception)
        }
        exception.receiver = toWho
    }

    private fun findNotFriendReceiver(exception: Exception): Friend {
        if (exception.toWho == metadataStore.user.id) {
            return metadataStore.user
        } else {
            return UNKNOWN_FRIEND
        }
    }

    private fun receiverIsUnknown(exception: Exception) = exception.receiver == null || exception.receiver.id == BigInteger.ZERO

    private fun otherIsNotFriend(fromWho: Friend) = fromWho.id == BigInteger("0")

    public fun bindExceptionImage(fromWho: Friend, toWho: Friend, imageView: ImageView) {
        if (metadataStore.user == fromWho) {
            if (metadataStore.user == toWho) {
                imageCache.setImageToView(metadataStore.user, imageView)
            } else {
                if (toWho.id != BigInteger.ZERO) {
                    imageCache.setImageToView(toWho, imageView)
                }
            }
        } else {
            if (fromWho.id != BigInteger.ZERO) {
                imageCache.setImageToView(fromWho, imageView)
            }
        }
    }

    public fun formattedExceptionDate(exception: Exception): String {
        return DateFormat.format("yyyy-MM-dd HH:mm:ss", exception.date.time).toString()
    }
}