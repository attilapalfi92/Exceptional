package com.attilapalfi.exceptional.ui.helpers

import android.text.format.DateFormat
import android.widget.ImageView
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.model.Friend
import com.attilapalfi.exceptional.persistence.FriendStore
import com.attilapalfi.exceptional.persistence.ImageCache
import com.attilapalfi.exceptional.persistence.MetadataStore
import java.math.BigInteger
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-03.
 */
public class ViewHelper {
    @Inject
    lateinit val friendStore: FriendStore
    @Inject
    lateinit val metadataStore: MetadataStore
    @Inject
    lateinit val imageCache: ImageCache

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
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
        var fromWho = exception.sender
        if (fromWho == null || fromWho.id == BigInteger.ZERO) {
            fromWho = friendStore.findFriendById(exception.fromWho)
            if (fromWho.id == BigInteger("0")) {
                fromWho = metadataStore.user
            }
            exception.sender = fromWho
        }
        return fromWho
    }

    public fun initExceptionReceiver(exception: Exception): Friend {
        var toWho = exception.receiver
        if (toWho == null || toWho.id == BigInteger.ZERO) {
            toWho = friendStore.findFriendById(exception.toWho)
            if (toWho.id == BigInteger("0")) {
                toWho = metadataStore.user
            }
            exception.receiver = toWho
        }
        return toWho
    }

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