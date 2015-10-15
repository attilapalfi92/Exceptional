package com.attilapalfi.exceptional.persistence

import android.content.Context
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.interfaces.ExceptionChangeListener
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.model.ExceptionFactory
import com.attilapalfi.exceptional.rest.messages.ExceptionInstanceWrapper
import io.paperdb.Book
import io.paperdb.Paper
import java.math.BigInteger
import java.util.*
import javax.inject.Inject


/**
 * Created by Attila on 2015-06-08.
 */
public class ExceptionInstanceStore {
    public var context: Context? = null
        @Inject
        public set
        get
    @Inject
    lateinit val exceptionTypeStore: ExceptionTypeStore
    @Inject
    lateinit val exceptionFactory: ExceptionFactory
    private lateinit val initThread: Thread
    private val database: Book
    private val exceptionChangeListeners = HashSet<ExceptionChangeListener>()
    private val storedExceptions = ArrayList<Exception>()
    private val idList = ArrayList<BigInteger>()
    private val geocoder: Geocoder
    private val handler: Handler

    companion object {
        private val STORE_SIZE = 1000
        private val INSTANCE_DATABASE = "INSTANCE_DATABASE"
        private val INSTANCE_IDs = "INSTANCE_IDs"
        private val EMPTY_EXCEPTION = Exception()
    }

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
        database = Paper.book(INSTANCE_DATABASE)
        handler = Handler(Looper.getMainLooper())
        geocoder = Geocoder(context, Locale.getDefault())
        initThread = Thread( {
            synchronized(storedExceptions) {
                idList.addAll(database.read(INSTANCE_IDs, LinkedList<BigInteger>()))
                idList.forEach {
                    val e = database.read(it.toString(), EMPTY_EXCEPTION)
                    e.exceptionType = exceptionTypeStore.findById(e.exceptionTypeId)
                    var index = Collections.binarySearch(storedExceptions, e)
                    if (index < 0) {
                        index = -index - 1
                        storedExceptions.add(index, e)
                    }
                }
            }
        } )
        initThread.start()
        notifyWhenInitFinished()
    }


    private fun notifyWhenInitFinished() {
        Thread({
            initThread.join()
            notifyListeners()
        }).start()
    }

    public fun wipe() {
        storedExceptions.clear()
        database.destroy()
        exceptionChangeListeners.forEach { it.onExceptionsChanged() }
    }

    public fun addExceptionAsync(exception: Exception) {
        if (!storedExceptions.contains(exception)) {
            object : AsyncTask<Void?, Void?, Void?>() {

                override fun doInBackground(vararg params: Void?): Void? {
                    saveToStore(exception)
                    return null
                }

                override fun onPostExecute(aVoid: Void?) {
                    notifyListeners()
                }

            }.execute()
        }
    }

    public fun addExceptionWithoutCity(exception: Exception) {
        initThread.join()
        addToListInOrder(exception)
    }

    public fun saveExceptionList(exceptionList: List<Exception>) {
        if (!exceptionList.isEmpty()) {
            storeEachIfNotContained(exceptionList)
            handler.post { this.notifyListeners() }
        }
    }

    public fun saveExceptionListAsync(exceptionList: List<Exception>) {
        if (!exceptionList.isEmpty()) {
            object : AsyncTask<Void?, Void?, Void?>() {

                override fun doInBackground(vararg params: Void?): Void? {
                    storeEachIfNotContained(exceptionList)
                    return null
                }

                override fun onPostExecute(aVoid: Void?) {
                    notifyListeners()
                }

            }.execute()
        }
    }

    fun setAnswered(instanceId: BigInteger, answered: Boolean) {
        val exception = findById(instanceId);
        if ( exception.instanceId != BigInteger.ZERO ) {
            exception.question.isAnswered = answered
            database.write(exception.instanceId.toString(), exception)
        }
    }

    public fun findById(id: BigInteger): Exception {
        initThread.join()
        synchronized(storedExceptions) {
            return storedExceptions.find { it.instanceId == id } ?: Exception()
        }
    }

    private fun saveToStore(e: Exception) {
        if (!storedExceptions.contains(e)) {
            saveWithCity(e)
            database.write(INSTANCE_IDs, idList)
        }
    }

    private fun storeEachIfNotContained(toBeStored: List<Exception>) {
        toBeStored.forEach { e ->
            if (!storedExceptions.contains(e)) {
                saveWithCity(e)
            }
        }
    }

    private fun saveWithCity(e: Exception) {
        setCityForException(e)
        addToListInOrder(e)
    }

    private fun addToListInOrder(e: Exception) {
        synchronized(storedExceptions) {
            var index = Collections.binarySearch(storedExceptions, e)
            if (index < 0) {
                index = -index - 1
                if (storedExceptions.size() >= STORE_SIZE) {
                    addNewOrKeepOld(e, index)
                } else {
                    addTheNewOne(e, index)
                }
            }
        }
    }

    private fun addNewOrKeepOld(e: Exception, index: Int) {
        val removeCandidate = storedExceptions.get(storedExceptions.size() - 1)
        if (removeCandidate.compareTo(e) > 0) {
            removeTheCandidate(storedExceptions)
            addTheNewOne(e, index)
        }
    }

    private fun removeTheCandidate(list: MutableList<Exception>) {
        val removed = list.remove(list.size() - 1)
        idList.remove(idList.size() - 1)
        database.delete(removed.instanceId.toString())
    }

    private fun addTheNewOne(e: Exception, index: Int) {
        storedExceptions.add(index, e)
        idList.add(index, e.instanceId)
        database.write(e.instanceId.toString(), e)
        database.write(INSTANCE_IDs, idList)
    }

    private fun setCityForException(e: Exception) {
        try {
            e.city = geocoder.getFromLocation(e.latitude, e.longitude, 1).get(0).locality
        } catch (exception: java.lang.Exception) {
            e.city = context!!.getString(R.string.unknown)
            exception.printStackTrace()
        }
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
        initThread.join()
        return idList
    }
}
