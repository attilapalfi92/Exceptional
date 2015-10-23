package com.attilapalfi.exceptional.rest

import android.content.Context
import com.attilapalfi.exceptional.dependency_injection.Injector
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
    public val globalThrowCounts by lazy { statRestInterface.getGlobalThrowCounts() }

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
    }
}