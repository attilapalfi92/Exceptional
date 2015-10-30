package com.attilapalfi.exceptional.rest

import android.content.Context
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.interfaces.GlobalThrowCountChangeListener
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
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

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
        async {
            globalThrowCounts = statRestInterface.getGlobalThrowCounts();
            uiThread {
                throwCountListeners.forEach { it.onGlobalThrowCountChanged() }
            }
        }
    }

    public fun addThrowCountListener(listener: GlobalThrowCountChangeListener) {
        throwCountListeners.add(listener)
    }

    public fun removeThrowCountListener(listener: GlobalThrowCountChangeListener) {
        throwCountListeners.remove(listener)
    }
}