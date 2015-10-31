package com.attilapalfi.exceptional.persistence

import android.os.Handler
import android.os.Looper
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.interfaces.FriendChangeListener
import com.attilapalfi.exceptional.model.Friend
import io.paperdb.Book
import io.paperdb.Paper
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import java.math.BigInteger
import java.util.*
import javax.inject.Inject

/**
 * Responsible for storing friends in the device's preferences
 * Created by Attila on 2015-06-12.
 */
public class FriendStore : AbstractStore() {

    @Inject
    lateinit var imageCache: ImageCache
    private val database: Book
    private val handler: Handler
    private val storedFriends = LinkedList<Friend>()
    private val idList = Collections.synchronizedList(LinkedList<BigInteger>())
    private val friendChangeListeners = HashSet<FriendChangeListener>()
    @Volatile
    override public var initialized = false

    companion object {
        private val FRIEND_DATABASE = "FRIEND_DATABASE"
        private val FRIEND_IDS = "FRIEND_IDS"
        private val EMPTY_FRIEND = Friend()
    }

    public fun addFriendChangeListener(listener: FriendChangeListener) {
        friendChangeListeners.add(listener)
    }

    public fun removeFriendChangeListener(listener: FriendChangeListener) {
        friendChangeListeners.remove(listener)
    }

    public fun getStoredFriends() = synchronized(storedFriends) { ArrayList(storedFriends) }

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
        database = Paper.book(FRIEND_DATABASE)
        handler = Handler(Looper.getMainLooper())
    }

    override public fun init() {
        synchronized(storedFriends) {
            idList.addAll(database.read(FRIEND_IDS, LinkedList<BigInteger>()))
            idList.forEach { storedFriends.add(database.read(it.toString(), EMPTY_FRIEND)) }
            Collections.sort(storedFriends)
            initialized = true
        }
        imageCache.loadImagesInitially(getStoredFriends())
    }

    public fun updateFriendList(friendList: List<Friend>) {
        waitTillInitialized()
        saveNewFriends(friendList)
        updateOldFriends(friendList)
        removeDeletedFriends(friendList)
        handler.post { notifyChangeListeners() }
    }

    public fun updateFriendPoints(id: BigInteger, points: Int) {
        async {
            waitTillInitialized()
            synchronized(storedFriends) {
                updatePointsById(id, points)
                Collections.sort(storedFriends)
            }
            uiThread {
                notifyChangeListeners()
            }
        }
    }

    public fun updatePointsOfFriends(points: Map<BigInteger, Int>) {
        waitTillInitialized()
        synchronized(storedFriends) {
            points.forEach { updatePointsById(it.key, it.value) }
            Collections.sort(storedFriends)
        }
        handler.post { this.notifyChangeListeners() }
    }

    public fun findById(friendId: BigInteger): Friend {
        waitTillInitialized()
        for (friend in getStoredFriends()) {
            if (friend.id == friendId) {
                return friend
            }
        }
        return Friend()
    }

    private fun updatePointsById(id: BigInteger, point: Int) {
        val friend = findById(id)
        if (point != friend.points) {
            friend.points = point
            database.write(id.toString(), friend)
        }
    }

    private fun saveNewFriends(friendList: List<Friend>) {
        val workList = ArrayList(friendList)
        synchronized(storedFriends) {
            workList.removeAll(storedFriends)
        }
        saveFriendList(workList)
    }

    private fun saveFriendList(friendList: List<Friend>) {
        imageCache.loadImagesInitiallyAsync(friendList)
        putFriendsToMemoryStore(friendList)
        writeFriendsToDb(friendList)
    }

    private fun putFriendsToMemoryStore(friendList: List<Friend>) {
        synchronized(storedFriends) {
            friendList.forEach {
                idList.add(it.id)
                it.imageDownloaded = true
            }
            storedFriends.addAll(friendList)
            Collections.sort(storedFriends)
        }
    }

    private fun writeFriendsToDb(friendList: List<Friend>) {
        friendList.forEach {
            database.write(it.id.toString(), it)
        }
        database.write(FRIEND_IDS, idList)
    }

    private fun updateOldFriends(friendList: List<Friend>) {
        val knownCurrentFriends = ArrayList<Friend>(friendList.size)
        knownCurrentFriends.addAll(friendList)
        var storedFriendsCopy = getStoredFriends()
        for (f1 in knownCurrentFriends) {
            for (f2 in storedFriendsCopy) {
                checkFriendChange(f1, f2)
            }
        }
    }

    private fun checkFriendChange(newFriendState: Friend, oldFriendState: Friend) {
        if (newFriendState == oldFriendState) {
            if (newFriendState.imageUrl != oldFriendState.imageUrl) {
                updateFriendInDatabase(newFriendState, oldFriendState)
                imageCache.updateImageAsync(newFriendState, oldFriendState)
            }
            if (newFriendState.getName() != oldFriendState.getName()) {
                updateFriendInDatabase(newFriendState, oldFriendState)
            }
        }
    }

    private fun updateFriendInDatabase(newState: Friend, oldInstance: Friend) {
        oldInstance.firstName = newState.firstName
        oldInstance.lastName = newState.lastName
        oldInstance.imageUrl = newState.imageUrl
        database.write(oldInstance.id.toString(), oldInstance)
    }

    private fun removeDeletedFriends(friendList: List<Friend>) {
        val deletedFriends = ArrayList<Friend>()
        deletedFriends.addAll(getStoredFriends())
        deletedFriends.removeAll(friendList)
        deleteFriends(deletedFriends)
    }

    private fun deleteFriends(toBeDeleted: List<Friend>) {
        deleteFriendsFromMemoryStore(toBeDeleted)
        deleteFriendsFromDb(toBeDeleted)
    }

    private fun deleteFriendsFromMemoryStore(toBeDeleted: List<Friend>) {
        synchronized(storedFriends) {
            toBeDeleted.forEach {
                storedFriends.remove(it)
                idList.remove(it.id)
            }
        }
    }

    private fun deleteFriendsFromDb(toBeDeleted: List<Friend>) {
        toBeDeleted.forEach {
            database.delete(it.id.toString())
        }
        database.write(FRIEND_IDS, idList)
    }

    private fun notifyChangeListeners() {
        if (Looper.myLooper() === Looper.getMainLooper()) {
            friendChangeListeners.forEach { it.onFriendsChanged() }
        }
    }

    public fun wipe() {
        storedFriends.clear()
        idList.clear()
        database.destroy()
        notifyChangeListeners()
    }
}
