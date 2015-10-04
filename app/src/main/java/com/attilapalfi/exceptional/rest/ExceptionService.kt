package com.attilapalfi.exceptional.rest

import android.content.Context
import android.widget.Toast
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.interfaces.ExceptionRefreshListener
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.model.ExceptionFactory
import com.attilapalfi.exceptional.model.Friend
import com.attilapalfi.exceptional.model.Question
import com.attilapalfi.exceptional.persistence.*
import com.attilapalfi.exceptional.rest.messages.BaseExceptionRequest
import com.attilapalfi.exceptional.rest.messages.ExceptionInstanceWrapper
import com.attilapalfi.exceptional.rest.messages.ExceptionRefreshResponse
import com.attilapalfi.exceptional.rest.messages.ExceptionSentResponse
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import javax.inject.Inject

/**
 * Created by Attila on 2015-06-13.
 */
public class ExceptionService {
    public var context: Context? = null
        @Inject
        set
        get
    public var restInterfaceFactory: RestInterfaceFactory? = null
        @Inject
        set
        get
    @Inject lateinit val exceptionInstanceStore: ExceptionInstanceStore
    @Inject lateinit val exceptionFactory: ExceptionFactory
    @Inject lateinit val friendStore: FriendStore
    @Inject lateinit val metadataStore: MetadataStore
    @Inject lateinit val questionStore: QuestionStore
    @Inject lateinit val exceptionTypeStore: ExceptionTypeStore
    private var exceptionRestInterface: ExceptionRestInterface? = null

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
        restInterfaceFactory?.let {
            exceptionRestInterface = it.create(context, ExceptionRestInterface::class.java)
        }
    }

    public fun throwException(exception: Exception, question: Question) {
        val exceptionInstanceWrapper = ExceptionInstanceWrapper(exception, question)
        try {
            exceptionRestInterface?.throwException(exceptionInstanceWrapper, object : Callback<ExceptionSentResponse> {
                override fun success(e: ExceptionSentResponse, response: Response) {
                    val toWho = friendStore.findFriendById(e.instanceWrapper.toWho)
                    metadataStore.points = e.yourPoints
                    friendStore.updateFriendPoints(e.instanceWrapper.toWho, e.friendsPoints)
                    exceptionInstanceStore.addExceptionAsync(exceptionFactory.createFromWrapper(e.instanceWrapper))
                    printSuccess(e, toWho)
                }

                private fun printSuccess(e: ExceptionSentResponse, toWho: Friend) {
                    Toast.makeText(context, e.exceptionShortName + " " + context?.getString(R.string.successfully_thrown)
                            + " " + toWho.getName(), Toast.LENGTH_SHORT).show()
                }

                override fun failure(error: RetrofitError) {
                    Toast.makeText(context, context?.getString(R.string.failed_to_throw_1) + error.getMessage(),
                            Toast.LENGTH_LONG).show()
                }
            })

        } catch (e: java.lang.Exception) {
            Toast.makeText(context, context?.getString(R.string.failed_to_throw_2) + e.getMessage(), Toast.LENGTH_LONG).show()
        }
    }

    public fun refreshExceptions(refreshListener: ExceptionRefreshListener) {
        val requestBody = BaseExceptionRequest(metadataStore.user.id,
                exceptionInstanceStore.getExceptionList().map { it.instanceId })

        exceptionRestInterface?.refreshExceptions(requestBody, object : Callback<ExceptionRefreshResponse> {
            override fun success(responseData: ExceptionRefreshResponse, response: Response) {
                if ( exceptionTypeStore.hasData() ) {
                    exceptionInstanceStore.saveExceptionListAsync(responseData.exceptionList)
                    questionStore.addQuestionListAsync(responseData.questionExceptions)
                    Toast.makeText(context, R.string.exceptions_syncd, Toast.LENGTH_SHORT).show()
                }
                refreshListener.onExceptionRefreshFinished()
            }

            override fun failure(error: RetrofitError) {
                Toast.makeText(context, context?.getString(R.string.failed_to_sync) + error.getMessage(), Toast.LENGTH_SHORT).show()
                refreshListener.onExceptionRefreshFinished()
            }
        })
    }
}
