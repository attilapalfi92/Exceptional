package com.attilapalfi.exceptional.facebook

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Friend
import com.attilapalfi.exceptional.persistence.*
import com.attilapalfi.exceptional.rest.AppStartRestConnector
import com.facebook.*
import com.facebook.login.LoginResult
import org.json.JSONArray
import org.json.JSONException
import java.math.BigInteger
import java.util.*
import javax.inject.Inject

/**
 * Created by Attila on 2015-06-06.
 */
class FacebookManager {
    @Inject
    lateinit var appStartRestConnector: AppStartRestConnector
    @Inject
    lateinit var exceptionInstanceStore: ExceptionInstanceStore
    @Inject
    lateinit var exceptionTypeStore: ExceptionTypeStore
    @Inject
    lateinit var friendStore: FriendStore
    @Inject
    lateinit var imageCache: ImageCache
    @Inject
    lateinit var metadataStore: MetadataStore
    private var accessToken: AccessToken? = null
    private lateinit var tokenTracker: AccessTokenTracker
    private var profile: Profile? = null
    private lateinit var profileTracker: ProfileTracker
    private val user = Friend()
    var callbackManager: CallbackManager? = null
        private set
    var facebookCallback: FacebookCallback<LoginResult>? = null
        private set
    private var loginSuccessHandler: FacebookLoginSuccessHandler? = null

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
    }

    fun onAppStart(application: Application) {
        initSubComponents(application)
        tokenTracker.startTracking()
        profileTracker.startTracking()
        accessToken = AccessToken.getCurrentAccessToken()
        if (accessToken != null) {
            refreshFriends()
        }
    }

    private fun initSubComponents(application: Application) {
        FacebookSdk.sdkInitialize(application.applicationContext)
        callbackManager = CallbackManager.Factory.create()
        initFacebookCallback()
        initTokenTracker()
        initProfileTracker()
    }

    private fun initProfileTracker() {
        profileTracker = object : ProfileTracker() {
            override fun onCurrentProfileChanged(oldProfile: Profile?, newProfile: Profile?) {
                if (newProfile != null) {
                    profile = newProfile
                }
            }
        }
    }

    private fun initTokenTracker() {
        tokenTracker = object : AccessTokenTracker() {
            override fun onCurrentAccessTokenChanged(oldToken: AccessToken?, newToken: AccessToken?) {
                accessToken = newToken
                if (newToken == null) {
                    setUserLoggedOut()
                } else {
                    refreshFriends()
                }
            }
        }
    }

    private fun setUserLoggedOut() {
        imageCache.wipe(friendStore.getStoredFriends())
        friendStore.wipe()
        exceptionInstanceStore.wipe()
        exceptionTypeStore.wipe()
        metadataStore.wipe()
    }

    private fun initFacebookCallback() {
        facebookCallback = object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult?) {
                accessToken = loginResult?.accessToken
                loginSuccessHandler?.onLoginSuccess(loginResult)
                initYourself()
                metadataStore.loggedIn = true
            }

            override fun onCancel() {
            }

            override fun onError(e: FacebookException?) {
            }
        }
    }

    private fun refreshFriends() {
        val request = GraphRequest.newMyFriendsRequest(accessToken) { jsonArray, graphResponse ->
            if (graphResponse.error == null) {
                Thread {
                    val friends = parseFriendsJson(jsonArray)
                    continueAppStart(friends)
                }.start()
            } else {
                if (metadataStore.loggedIn) {
                    Log.e("FacebookManager: ", "GraphRequest error: " + graphResponse.error)
                }
            }
        }
        executeGraphRequest(request)
    }

    private fun executeGraphRequest(request: GraphRequest) {
        val parameters = Bundle()
        parameters.putString("fields", "id,name,picture")
        request.parameters = parameters
        request.executeAsync()
    }

    private fun parseFriendsJson(jsonArray: JSONArray): List<Friend> {
        Log.d("response length: ", Integer.toString(jsonArray.length()))
        val friends = ArrayList<Friend>()
        for (i in 0..jsonArray.length() - 1) {
            try {
                val friend = parseFriend(jsonArray, i)
                friends.add(friend)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
        return friends
    }

    private fun continueAppStart(friends: List<Friend>) {
        if (metadataStore.loggedIn) {
            initYourself()
            metadataStore.updateUser(user)
            friendStore.updateFriendList(friends)
            if (!metadataStore.firstStartFinished) {
                appStartRestConnector.onFirstAppStart(friends, user.id)
            } else {
                appStartRestConnector.onRegularAppStart(friends, user.id)
            }
        }
    }

    private fun initYourself() {
        profile = Profile.getCurrentProfile()
        if (profile != null) {
            user.id = BigInteger(profile!!.id)
            user.firstName = (profile!!.firstName.trim { it <= ' ' } + " " + profile!!.middleName.trim { it <= ' ' }).trim { it <= ' ' }
            user.lastName = profile!!.lastName
            user.imageUrl = profile!!.getProfilePictureUri(200, 200).toString()
            user.points = 100
            user.imageDownloaded = false
        }
    }

    @Throws(JSONException::class)
    private fun parseFriend(jsonArray: JSONArray, i: Int): Friend {
        val user = jsonArray.getJSONObject(i)
        val names = parseFirstAndLastName(user.getString("name"))
        val id = user.getString("id")
        val imageData = user.getJSONObject("picture").getJSONObject("data")
        val imageUrl = imageData.getString("url")
        return Friend(BigInteger(id), names[0], names[1], imageUrl, 100, false)
    }

    private fun parseFirstAndLastName(name: String): Array<String> {
        val names = name.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val firstAndLastName = arrayOf("", "")
        firstAndLastName[1] = names[names.size - 1]
        for (i in 0..names.size - 1 - 1) {
            firstAndLastName[0] += names[i] + " "
        }
        firstAndLastName[0] = firstAndLastName[0].trim { it <= ' ' }
        return firstAndLastName
    }

    fun onAppKilled() {
        tokenTracker.stopTracking()
        profileTracker.stopTracking()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Boolean {
        return callbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

    fun registerLoginSuccessHandler(handler: FacebookLoginSuccessHandler) {
        loginSuccessHandler = handler
    }
}
