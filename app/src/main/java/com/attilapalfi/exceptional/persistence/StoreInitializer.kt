package com.attilapalfi.exceptional.persistence

import android.app.Application
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.facebook.FacebookManager
import org.jetbrains.anko.async
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-31.
 */
public class StoreInitializer {

    @Inject
    lateinit var instanceStore: ExceptionInstanceStore
    @Inject
    lateinit var typeStore: ExceptionTypeStore
    @Inject
    lateinit var questionStore: QuestionStore
    @Inject
    lateinit var friendStore: FriendStore
    @Inject
    lateinit var imageCache: ImageCache
    @Inject
    lateinit var metadataStore: MetadataStore
    @Inject
    lateinit var facebookManager: FacebookManager

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
    }

    public fun initialize(application: Application) {
        async {
            metadataStore.init()
            friendStore.init()
            typeStore.init()
            instanceStore.init()
            questionStore.init()
            facebookManager.onAppStart(application)
        }
    }
}