package com.attilapalfi.exceptional.persistence

/**
 * Created by palfi on 2015-10-31.
 */
abstract class AbstractStore {
    @Volatile
    public abstract var initialized: Boolean

    public abstract fun init()

    protected fun waitTillInitialized() {
        while (!initialized) {
            Thread.sleep(50)
        }
    }
}