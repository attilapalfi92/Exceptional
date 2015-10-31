package com.attilapalfi.exceptional.persistence

import android.content.Context
import android.location.Geocoder
import android.os.Handler
import android.os.Looper
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.interfaces.ExceptionChangeListener
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.model.ExceptionFactory
import com.attilapalfi.exceptional.model.ExceptionHelper
import com.attilapalfi.exceptional.rest.messages.ExceptionInstanceWrapper
import io.paperdb.Book
import io.paperdb.Paper
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import java.math.BigInteger
import java.util.*
import javax.inject.Inject


/**
 * Created by Attila on 2015-06-08.
 */
public class ExceptionInstanceStore : AbstractStore() {
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var exceptionTypeStore: ExceptionTypeStore
    @Inject
    lateinit var exceptionFactory: ExceptionFactory
    @Inject
    lateinit var exceptionHelper: ExceptionHelper
    private val database: Book
    private val exceptionChangeListeners = HashSet<ExceptionChangeListener>()
    private val storedExceptions = ArrayList<Exception>()
    private val idList = ArrayList<BigInteger>()
    private val handler: Handler

    @Volatile
    override var initialized = false

    companion object {
        private val STORE_SIZE = 10000
        private val INSTANCE_DATABASE = "INSTANCE_DATABASE"
        private val INSTANCE_IDs = "INSTANCE_IDs"
        private val EMPTY_EXCEPTION = Exception()
    }

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
        database = Paper.book(INSTANCE_DATABASE)
        handler = Handler(Looper.getMainLooper())
    }

    override public fun init() {
        synchronized(storedExceptions) {
            idList.addAll(database.read(INSTANCE_IDs, LinkedList<BigInteger>()))
            idList.forEach {
                val e = database.read(it.toString(), EMPTY_EXCEPTION)
                e.exceptionType = exceptionTypeStore.findById(e.exceptionTypeId)
                exceptionHelper.initExceptionSender(e)
                exceptionHelper.initExceptionReceiver(e)
                var index = Collections.binarySearch(storedExceptions, e)
                if (index < 0) {
                    index = -index - 1
                    storedExceptions.add(index, e)
                }
            }
            initialized = true
        }
        notifyListeners()
    }

    public fun wipe() {
        storedExceptions.clear()
        database.destroy()
        exceptionChangeListeners.forEach { it.onExceptionsChanged() }
    }

    public fun addExceptionAsync(exception: Exception) {
        async {
            waitTillInitialized()
            storeException(exception)
            uiThread {
                notifyListeners()
            }
        }
    }

    public fun addException(exception: Exception) {
        waitTillInitialized()
        storeException(exception)
        notifyListeners()
    }

    public fun saveExceptionList(exceptionList: List<Exception>) {
        if (!exceptionList.isEmpty()) {
            storeExceptionList(exceptionList)
            handler.post { this.notifyListeners() }
        }
    }

    public fun saveExceptionListAsync(exceptionList: List<Exception>) {
        if (!exceptionList.isEmpty()) {
            async {
                storeExceptionList(exceptionList)
                uiThread {
                    notifyListeners()
                }
            }
        }
    }

    public fun setAnswered(instanceWrapper: ExceptionInstanceWrapper, answered: Boolean) {
        val exception = findById(instanceWrapper.instanceId);
        if ( exception.instanceId != BigInteger.ZERO ) {
            exception.pointsForSender = instanceWrapper.pointsForSender
            exception.pointsForReceiver = instanceWrapper.pointsForReceiver
            exception.question.answered = answered
            database.write(exception.instanceId.toString(), exception)
        }
        notifyListeners()
    }

    public fun findById(id: BigInteger): Exception {
        waitTillInitialized()
        synchronized(storedExceptions) {
            return storedExceptions.find { it.instanceId == id } ?: Exception()
        }
    }

    private fun storeException(e: Exception) {
        addToListInOrder(e)
        database.write(INSTANCE_IDs, idList)
    }

    private fun storeExceptionList(toBeStored: List<Exception>) {
        toBeStored.forEach { e -> addToListInOrder(e) }
        database.write(INSTANCE_IDs, idList)
    }

    private fun addToListInOrder(e: Exception) {
        synchronized(storedExceptions) {
            if (!storedExceptions.contains(e)) {
                var index = Collections.binarySearch(storedExceptions, e)
                if (index < 0) {
                    index = -index - 1
                    if (storedExceptions.size >= STORE_SIZE) {
                        addNewOrKeepOld(e, index)
                    } else {
                        addTheNewOne(e, index)
                    }
                }
            }
        }
    }

    private fun addNewOrKeepOld(e: Exception, index: Int) {
        val removeCandidate = storedExceptions[storedExceptions.size - 1]
        if (removeCandidate.compareTo(e) > 0) {
            removeTheCandidate(storedExceptions)
            addTheNewOne(e, index)
        }
    }

    private fun removeTheCandidate(list: MutableList<Exception>) {
        val removed = list.removeAt(list.size - 1)
        idList.removeAt(idList.size - 1)
        database.delete(removed.instanceId.toString())
    }

    private fun addTheNewOne(e: Exception, index: Int) {
        storedExceptions.add(index, e)
        idList.add(index, e.instanceId)
        database.write(e.instanceId.toString(), e)
    }

    private fun notifyListeners() {
        if (Looper.myLooper() === Looper.getMainLooper()) {
            exceptionChangeListeners.forEach { it.onExceptionsChanged() }
        } else {
            handler.post {
                exceptionChangeListeners.forEach { it.onExceptionsChanged() }
            }
        }
    }

    public fun getExceptionList() = synchronized(storedExceptions) { ArrayList(storedExceptions) }

    public fun addExceptionChangeListener(listener: ExceptionChangeListener): Boolean {
        return exceptionChangeListeners.add(listener)
    }

    public fun removeExceptionChangeListener(listener: ExceptionChangeListener): Boolean {
        return exceptionChangeListeners.remove(listener)
    }

    public fun getKnownIds(): ArrayList<BigInteger> {
        waitTillInitialized()
        return idList
    }
}
