package com.attilapalfi.exceptional.rest

import android.content.Context
import android.os.Build
import android.widget.Toast
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.model.ExceptionQuestion
import com.attilapalfi.exceptional.model.Friend
import com.attilapalfi.exceptional.persistence.*
import com.attilapalfi.exceptional.rest.messages.AppStartRequest
import com.attilapalfi.exceptional.rest.messages.AppStartResponse
import com.attilapalfi.exceptional.rest.messages.ExceptionInstanceWrapper
import com.google.android.gms.gcm.GoogleCloudMessaging
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import java.io.IOException
import java.math.BigInteger
import javax.inject.Inject

/**
 * Created by palfi on 2015-09-12.
 */
public class AppStartService {
    @Inject
    lateinit val context: Context
    @Inject
    lateinit val exceptionInstanceStore: ExceptionInstanceStore
    @Inject
    lateinit val exceptionTypeStore: ExceptionTypeStore
    @Inject
    lateinit val friendStore: FriendStore
    @Inject
    lateinit val metadataStore: MetadataStore
    @Inject
    lateinit val restInterfaceFactory: RestInterfaceFactory
    @Inject
    lateinit val questionStore: QuestionStore
    private var projectNumber: String = ""
    private var appStartRestInterface: AppStartRestInterface? = null
    private var googleCloudMessaging: GoogleCloudMessaging? = null
    private var registrationId: String = ""
    public var androidId: String = ""
    private val requestBody = AppStartRequest()

    init {
        init()
    }

    private fun init() {
        Injector.INSTANCE.applicationComponent.inject(this)
        projectNumber = context.getString(R.string.project_number)
        appStartRestInterface = restInterfaceFactory.create(context, AppStartRestInterface::class.java)
    }

    public fun onFirstAppStart(friendList: List<Friend>, profileId: BigInteger) {
        initRequestBody(friendList, profileId)
        requestBody.deviceName = getDeviceName()
        gcmFirstAppStart()
    }

    public fun onRegularAppStart(friendList: List<Friend>, profileId: BigInteger) {
        initRequestBody(friendList, profileId)
        requestBody.exceptionVersion = metadataStore.exceptionVersion
        try {
            appStartRestInterface?.regularAppStart(requestBody, object : Callback<AppStartResponse> {
                override fun success(responseBody: AppStartResponse, response: Response) {
                    saveCommonData(responseBody)
                }

                override fun failure(error: RetrofitError) {
                    Toast.makeText(context, context.getString(R.string.failed_to_connect) + error.getMessage(),
                            Toast.LENGTH_LONG).show()
                }
            })

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    private fun initRequestBody(friendList: List<Friend>, profileId: BigInteger) {
        requestBody.deviceId = androidId
        requestBody.userFacebookId = profileId
        requestBody.friendsFacebookIds = friendList.map { it.id }
        requestBody.knownExceptionIds = exceptionInstanceStore.getKnownIds()
        requestBody.firstName = metadataStore.user.firstName
        requestBody.lastName = metadataStore.user.lastName
    }

    private fun gcmFirstAppStart() {
        googleCloudMessaging = GoogleCloudMessaging.getInstance(context)
        try {
            registrationId = googleCloudMessaging!!.register(projectNumber)
        } catch (e: IOException) {
            Toast.makeText(context, context.getString(R.string.failed_to_connect_to_gcm_servers) + e.getMessage(), Toast.LENGTH_LONG).show()
        }

        if (registrationId != "") {
            requestBody.gcmId = registrationId
            backendFirstAppStart()
        }
    }

    private fun backendFirstAppStart() {
        try {
            appStartRestInterface?.firstAppStart(requestBody, object : Callback<AppStartResponse> {
                override fun success(responseBody: AppStartResponse, response: Response) {
                    saveCommonData(responseBody)
                    metadataStore.isFirstStartFinished = true
                }

                override fun failure(error: RetrofitError) {
                    Toast.makeText(context, context.getString(R.string.failed_to_connect_3) + error.getMessage(),
                            Toast.LENGTH_LONG).show()
                }
            })
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    private fun saveCommonData(responseBody: AppStartResponse) {
        Thread {
            if (responseBody.exceptionVersion > metadataStore.exceptionVersion) {
                exceptionTypeStore.addExceptionTypes(responseBody.exceptionTypes)
            }
            friendStore.updatePointsOfFriends(responseBody.friendsPoints)
            metadataStore.points = responseBody.points
            metadataStore.isSubmittedThisWeek = responseBody.submittedThisWeek
            metadataStore.isVotedThisWeek = responseBody.votedThisWeek
            metadataStore.exceptionVersion = responseBody.exceptionVersion
            exceptionTypeStore.setVotedExceptionTypes(responseBody.beingVotedTypes)
            storeQuestions(responseBody.myExceptions)
            exceptionInstanceStore.saveExceptionList(responseBody.myExceptions)
        }.start()
    }

    private fun storeQuestions(exceptions: List<ExceptionInstanceWrapper>) {
        val questionList = exceptions.filter { it.question.hasQuestion && !it.question.isAnswered }
                .map { ExceptionQuestion(it.question, Exception(it, exceptionTypeStore.findById(it.exceptionTypeId))) }
        questionStore.addQuestionList(questionList)
    }

    public fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        if (model.startsWith(manufacturer)) {
            return capitalize(model)
        } else {
            return capitalize(manufacturer) + " " + model
        }
    }

    private fun capitalize(s: String?): String {
        if (s == null || s.length() == 0) {
            return ""
        }
        val first = s.charAt(0)
        if (Character.isUpperCase(first)) {
            return s
        } else {
            return Character.toUpperCase(first) + s.substring(1)
        }
    }
}
