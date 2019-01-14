package com.android.arch.auth.facebook

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.android.arch.auth.core.data.entity.SocialNetworkType.FACEBOOK
import com.android.arch.auth.core.data.network.NetworkSignInService
import com.android.arch.auth.core.data.network.ParamsBundle
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

/**
 * Created by alexk on 12/19/18.
 * Project android-auth-pack
 */
class FacebookSignInService : NetworkSignInService<AccessToken>() {

    override val socialNetworkType = FACEBOOK

    private val callbackManager: CallbackManager by lazy { CallbackManager.Factory.create() }
    private val loginManager: LoginManager = LoginManager.getInstance()

    /**
     * Should be called in Activity.onCreate method
     */
    override fun onCreate(activity: ComponentActivity) {
        super.onCreate(activity)
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

            override fun onSuccess(result: LoginResult) = with(result) {
                facebookSignInCallback?.let { fetchProfile(accessToken) } ?: postResult(accessToken, null)
            }

            override fun onError(exception: FacebookException) = handleFacebookSignInError(exception)

            override fun onCancel() = handleFacebookSignInError(FacebookOperationCanceledException("canceled"))

        })
    }

    /**
     * Should be called in Activity.onDestroy method
     */
    override fun onDestroy() {
        super.onDestroy()
        loginManager.unregisterCallback(callbackManager)
        facebookSignInCallback = null
    }

    override fun signIn(activity: Activity) {
        loginManager.logInWithReadPermissions(activity, READ_PERMISSIONS)
    }

    override fun signOut() {
        loginManager.logOut()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun getParamsBundle(data: AccessToken) = ParamsBundle(data.token)

    /**
     * Sign in User with Facebook via two steps:
     *     - fetch facebook accessToken
     *     - fetch facebook userProfile
     *
     * Exceptions:
     *    FacebookOperationCanceledException - when request was canceled
     *    FacebookException - in other cases. See Facebook SDK
     *
     */
    fun signIn(callback: FacebookSignInCallback) {
        activity?.apply {
            facebookSignInCallback = callback
            signIn(this)
        } ?: Log.w("signIn", "not attached. activity = null")
    }

    private fun handleFacebookSignInError(e: Exception) {
        facebookSignInCallback?.let { postFacebookSignInResult(FacebookSignInResponse(exception = e)) } ?: postResult(null, e)
    }

    private fun postFacebookSignInResult(data: FacebookSignInResponse) {
        facebookSignInCallback
            ?.let { callback -> callback(data) }
            ?: Log.w("postResult", "Wrong state. facebookSignInCallback = null")
    }

    private fun fetchProfile(token: AccessToken?) {
        token?.let {
            GraphRequest.newMeRequest(token) { _, response ->
                postFacebookSignInResult(response.toFacebookSignInResponse())
            }.apply {
                parameters = Bundle().apply { putString(PARAMS_KEY, PERMISSIONS) }
                executeAsync()
            }
        } ?: handleFacebookSignInError(FacebookAuthorizationException("Failed fetch profile. Token undefined"))
    }

    private fun GraphResponse.toFacebookSignInResponse(): FacebookSignInResponse {
        val profile = try {
            jsonObject?.let {
                FacebookProfile(
                    id = it.getString(ID),
                    name = it.getString(NAME),
                    email = it.getString(EMAIL),
                    firstName = it.getString(FIRST_NAME),
                    lastName = it.getString(LAST_NAME),
                    picture = it.getJSONObject(PICTURE)?.getJSONObject(DATA)?.getString(URL)
                )
            }
        } catch (e: Exception) {
            Log.e("FacebookSignInResponse", "Failed wrapping response", e)
            null
        }

        return FacebookSignInResponse(profile, error?.exception)
    }

    private var facebookSignInCallback: FacebookSignInCallback? = null

    companion object {
        private const val PARAMS_KEY = "fields"
        private const val ID = "id"
        private const val URL = "url"
        private const val DATA = "data"
        private const val NAME = "name"
        private const val FIRST_NAME = "first_name"
        private const val LAST_NAME = "last_name"
        private const val EMAIL = "email"
        private const val PICTURE = "picture"
        private const val PERMISSIONS = "$ID,$NAME,$FIRST_NAME,$LAST_NAME,$EMAIL,$PICTURE.width(200)"

        private val READ_PERMISSIONS = listOf("email", "public_profile")
    }
}