package com.attilapalfi.exceptional.model

import java.math.BigInteger

/**
 * Created by palfi on 2015-10-03.
 */
public data class Friend(
        @Volatile public var id: BigInteger = BigInteger.ZERO,
        @Volatile public var firstName: String = "",
        @Volatile public var lastName: String = "",
        @Volatile public var imageUrl: String = "",
        @Volatile public var points: Int = 100,
        @Volatile public var imageDownloaded: Boolean = false) : Comparable<Friend> {

    override fun compareTo(other: Friend) = other.points.compareTo(points)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        return (other as Friend).id == id
    }

    public fun isImageDownloaded() = imageDownloaded

    public fun getName() = firstName + " " + lastName
}