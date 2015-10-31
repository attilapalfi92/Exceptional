package com.attilapalfi.exceptional.persistence

import android.os.Handler
import android.os.Looper
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.interfaces.VotedTypeListener
import com.attilapalfi.exceptional.model.ExceptionType
import io.paperdb.Book
import io.paperdb.Paper
import java.util.*

/**
 * Created by Attila on 2015-06-09.
 */
public class ExceptionTypeStore : AbstractStore() {
    private val MAX_ID = "maxId"
    private val HAS_DATA = "hasData"
    @Volatile
    private var hasData = false
    @Volatile
    override public var initialized = false

    private val database: Book
    private val handler: Handler
    private val exceptionTypeStore = Collections.synchronizedMap(HashMap<String, LinkedList<ExceptionType>>())
    private val votedExceptionTypeList = Collections.synchronizedList(LinkedList<ExceptionType>())
    private val votedTypeListeners = HashSet<VotedTypeListener>()

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
        database = Paper.book(TYPE_DATABASE)
        handler = Handler(Looper.getMainLooper())

    }

    override public fun init() {
        synchronized (exceptionTypeStore) {
            if (database.read(HAS_DATA, false)) {
                initExceptionTypeStore()
                sortExceptionStore()
                hasData = true
            }
            initialized = true
        }
    }

    public fun addExceptionTypes(exceptionTypes: List<ExceptionType>) {
        synchronized (exceptionTypeStore) {
            saveExceptionTypeStore(exceptionTypes)
        }
        hasData = true
        database.write(HAS_DATA, true)
    }

    private fun saveExceptionTypeStore(exceptionTypes: List<ExceptionType>) {
        var maxId = 0
        for (exception in exceptionTypes) {
            if (!exceptionTypeStore.containsKey(exception.type)) {
                exceptionTypeStore.put(exception.type, LinkedList<ExceptionType>())
            }
            exceptionTypeStore[exception.type]?.add(exception)
            database.write(Integer.toString(exception.id), exception)
            maxId = if (exception.id > maxId) exception.id else maxId
        }
        database.write(MAX_ID, maxId)
        sortExceptionStore()
    }

    public fun setVotedExceptionTypes(exceptionTypes: List<ExceptionType>) {
        synchronized (votedExceptionTypeList) {
            votedExceptionTypeList.clear()
            votedExceptionTypeList.addAll(exceptionTypes)
            sortVotedExceptionList()
        }
        handler.post { this.notifyVotedTypeListeners() }
    }

    private fun initExceptionTypeStore() {
        val minId = 1
        val maxId = database.read(MAX_ID, 0)
        for (i in minId..maxId) {
            val exceptionType = database.read(Integer.toString(i), EMPTY_TYPE)
            if (!exceptionTypeStore.containsKey(exceptionType.type)) {
                exceptionTypeStore.put(exceptionType.type, LinkedList<ExceptionType>())
            }
            exceptionTypeStore[exceptionType.type]?.add(exceptionType)
        }
    }

    private fun sortExceptionStore() {
        for (typeList in exceptionTypeStore.values) {
            Collections.sort(typeList, ExceptionType.ShortNameComparator())
        }
    }

    private fun sortVotedExceptionList() {
        Collections.sort(votedExceptionTypeList, ExceptionType.VoteComparator())
    }

    public fun findById(id: Int): ExceptionType {
        waitTillInitialized()
        synchronized(exceptionTypeStore) {
            val tempMap = HashMap(exceptionTypeStore)
            for (exceptionTypeList in ArrayList(tempMap.values)) {
                for (exceptionType in exceptionTypeList) {
                    if (id == exceptionType.id) {
                        return exceptionType
                    }
                }
            }
            return ExceptionType()
        }
    }

    public fun getExceptionTypes(): Set<String> {
        synchronized(exceptionTypeStore) {
            val tempMap = HashMap(exceptionTypeStore)
            return HashSet(tempMap.keys)
        }
    }

    public fun getExceptionTypeListByName(typeName: String): List<ExceptionType> {
        synchronized(exceptionTypeStore) {
            val tempMap = HashMap(exceptionTypeStore)
            if (tempMap.containsKey(typeName)) {
                return ArrayList(tempMap[typeName])
            }
            return ArrayList()
        }
    }

    public fun getVotedExceptionTypeList(): List<ExceptionType> {
        synchronized(exceptionTypeStore) {
            return ArrayList(votedExceptionTypeList)
        }
    }

    public fun updateVotedType(votedType: ExceptionType) {
        synchronized (votedExceptionTypeList) {
            val index = votedExceptionTypeList.indexOf(votedType)
            val listElementException = votedExceptionTypeList.get(index)
            listElementException.voteCount = votedType.voteCount
        }
        notifyVotedTypeListeners()
    }

    public fun addVotedType(submittedType: ExceptionType) {
        synchronized (votedExceptionTypeList) {
            votedExceptionTypeList.add(submittedType)
        }
        notifyVotedTypeListeners()
    }

    public fun notifyVotedTypeListeners() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            votedTypeListeners.forEach { it.onVoteListChanged() }
        }
    }

    public fun addVotedTypeListener(listener: VotedTypeListener): Boolean {
        return votedTypeListeners.add(listener)
    }

    public fun removeVotedTypeListener(listener: VotedTypeListener): Boolean {
        return votedTypeListeners.remove(listener)
    }

    public fun hasData(): Boolean {
        return hasData
    }

    public fun wipe() {
        exceptionTypeStore.clear()
        votedExceptionTypeList.clear()
        database.destroy()
    }

    companion object {
        private val TYPE_DATABASE = "TYPE_DATABASE"
        private val EMPTY_TYPE = ExceptionType(0, "", "", "")
    }
}
