package com.attilapalfi.exceptional.rest

import android.content.Context
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.interfaces.GlobalPointsChangeListener
import com.attilapalfi.exceptional.interfaces.GlobalThrowCountChangeListener
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import java.math.BigInteger
import java.util.*
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-23.
 */
public class StatSupplier {
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var restInterfaceFactory: RestInterfaceFactory
    private val statRestInterface by lazy { restInterfaceFactory.create(context, StatRestInterface::class.java) }
    @Volatile
    public var globalThrowCounts = LinkedHashMap<Int, Long>()
    private val throwCountListeners = HashSet<GlobalThrowCountChangeListener>()
    @Volatile
    public var globalPoints = LinkedHashMap<BigInteger, Int>()
    private val globalPointsListeners = HashSet<GlobalPointsChangeListener>()

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
        async {
            globalThrowCounts = statRestInterface.getGlobalThrowCounts();
            globalPoints = statRestInterface.getGlobalPoints();
            uiThread {
                throwCountListeners.forEach { it.onGlobalThrowCountChanged() }
                globalPointsListeners.forEach { it.onGlobalPointsChanged() }
            }
        }
    }

    public fun addThrowCountListener(listener: GlobalThrowCountChangeListener) {
        throwCountListeners.add(listener)
    }

    public fun removeThrowCountListener(listener: GlobalThrowCountChangeListener) {
        throwCountListeners.remove(listener)
    }

    public fun addGlobalPointsListener(listener: GlobalPointsChangeListener) {
        globalPointsListeners.add(listener)
    }

    public fun removeGlobalPointsListener(listener: GlobalPointsChangeListener) {
        globalPointsListeners.remove(listener)
    }
}