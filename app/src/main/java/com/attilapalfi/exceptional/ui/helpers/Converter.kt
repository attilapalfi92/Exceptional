package com.attilapalfi.exceptional.ui.helpers

import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.model.Friend
import com.attilapalfi.exceptional.persistence.FriendStore
import java.math.BigInteger
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-03.
 */
public class Converter {
    @Inject
    lateinit val friendStore: FriendStore

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
    }

    public fun getNameAndCity(exception: Exception, fromWho: Friend): String {
        var senderName = fromWho.getName()
        return addCity(senderName, exception.city)
    }

    private fun getSenderName(exception: Exception): String {
        var senderName = ""
        if (exception.fromWho != BigInteger.ZERO ) {
            senderName = friendStore.findFriendById(exception.fromWho).getName()
        }
        return senderName
    }

    private fun addCity(senderName: String, city: String): String {
        var nameAndCity = senderName
        if ("" != city) {
            nameAndCity += (", " + city)
        }
        return nameAndCity
    }
}