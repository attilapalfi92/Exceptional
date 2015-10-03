package com.attilapalfi.exceptional.model

import java.math.BigInteger

/**
 * Created by palfi on 2015-10-03.
 */
public data class Friend(
        public var id: BigInteger = BigInteger.ZERO,
        public var firstName: String = "",
        public var lastName: String = "",
        public var imageUrl: String = "",
        public var points: Int = 100,
        public var imageLoaded: Boolean = false) : Comparable<Friend> {

    override fun compareTo(o: Friend) = o.points.compareTo(points)

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        return (o as Friend).id == id
    }

    public fun isImageLoaded() = imageLoaded

    public fun getName() = firstName + " " + lastName
}