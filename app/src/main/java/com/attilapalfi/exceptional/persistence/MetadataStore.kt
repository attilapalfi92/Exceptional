package com.attilapalfi.exceptional.persistence

import android.os.Handler
import android.os.Looper
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.interfaces.PointChangeListener
import com.attilapalfi.exceptional.model.Friend
import io.paperdb.Book
import io.paperdb.Paper
import java.math.BigInteger
import java.util.*
import javax.inject.Inject

/**
 * Created by palfi on 2015-08-21.
 */
public class MetadataStore : AbstractStore() {

    @Inject
    lateinit var imageCache: ImageCache
    private val database: Book
    private val handler: Handler
    @Volatile
    override public var initialized = false

    @Volatile
    public var exceptionVersion: Int = 0
        public set(value) {
            if (this.exceptionVersion != value) {
                field = value
                database.write(EXCEPTION_VERSION, exceptionVersion)
            }
        }

    @Volatile
    public var loggedIn = false
        public set(value) {
            if (this.loggedIn != value) {
                field = value
                database.write(LOGGED_IN, value)
            }
        }

    @Volatile
    public var firstStartFinished = false
        public set(value) {
            if (this.firstStartFinished != value) {
                field = value
                database.write(FIRST_START_FINISHED, value)
            }
        }

    @Volatile
    public var votedThisWeek = true
        public set(value) {
            if (this.votedThisWeek != value) {
                field = value
                database.write(VOTED_THIS_WEEK, value)
            }
        }

    @Volatile
    public var submittedThisWeek = true
        public set(value) {
            if (this.submittedThisWeek != value) {
                field = value
                database.write(SUBMITTED_THIS_WEEK, value)
            }
        }

    @Volatile
    public var user: Friend = EMPTY_USER
        public set(value) {
            value.imageDownloaded = true
            field = value
            database.write(USER, value)
        }

    private val pointChangeListeners = HashSet<PointChangeListener>()

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
        database = Paper.book(METADATA_DATABASE)
        handler = Handler(Looper.getMainLooper())
    }

    override public fun init() {
        exceptionVersion = database.read(EXCEPTION_VERSION, exceptionVersion)
        loggedIn = database.read(LOGGED_IN, loggedIn)
        firstStartFinished = database.read(FIRST_START_FINISHED, firstStartFinished)
        votedThisWeek = database.read(VOTED_THIS_WEEK, votedThisWeek)
        submittedThisWeek = database.read(SUBMITTED_THIS_WEEK, submittedThisWeek)
        user = database.read(USER, EMPTY_USER)
        initialized = true
        notifyPointListeners()
    }

    public fun setPoints(points: Int) {
        if (user.points != points) {
            user.points = points
            database.write(USER, user)
            if (Looper.myLooper() == Looper.getMainLooper()) {
                pointChangeListeners.forEach { it.onPointsChanged() }
            } else {
                handler.post { pointChangeListeners.forEach { it.onPointsChanged() } }
            }
        }
    }

    public fun getPoints(): Int {
        return user.points
    }

    public fun isItUser(friend: Friend): Boolean {
        return friend == user
    }


    public fun updateUser(newUserState: Friend) {
        newUserState.imageDownloaded = true
        lookForChange(newUserState)
    }

    private fun lookForChange(newUserState: Friend) {
        var changed = false
        if (newUserState.imageUrl != user.imageUrl) {
            changed = true
            imageCache.updateImageAsync(newUserState, user)
        }
        if (newUserState.getName() != user.getName()) {
            changed = true
        }
        if (changed) {
            user = newUserState
        }
    }

    public fun wipe() {
        exceptionVersion = 0
        loggedIn = false
        firstStartFinished = false
        database.destroy()
    }

    private fun notifyPointListeners() {
        if (Looper.myLooper() === Looper.getMainLooper()) {
            pointChangeListeners.forEach { it.onPointsChanged() }
        } else {
            handler.post {
                pointChangeListeners.forEach { it.onPointsChanged() }
            }
        }
    }

    public fun addPointChangeListener(listener: PointChangeListener): Boolean {
        return pointChangeListeners.add(listener)
    }

    public fun removePointChangeListener(listener: PointChangeListener): Boolean {
        return pointChangeListeners.remove(listener)
    }

    companion object {
        private val METADATA_DATABASE = "METADATA_DATABASE"
        private val EXCEPTION_VERSION = "exceptionVersion"
        private val LOGGED_IN = "loggedIn"
        private val FIRST_START_FINISHED = "firstStartFinished"
        private val VOTED_THIS_WEEK = "votedThisWeek"
        private val SUBMITTED_THIS_WEEK = "submittedThisWeek"
        private val USER = "user"
        private val EMPTY_USER = Friend(BigInteger("0"), "", "", "", 100, false)
    }
}
