package com.android.arch.auth.facebook

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.android.arch.auth.core.data.entity.AuthError
import com.android.arch.auth.core.data.entity.AuthError.CanceledAuthError
import com.android.arch.auth.core.data.entity.AuthError.ServiceAuthError
import com.android.arch.auth.core.data.entity.AuthUserProfile
import com.android.arch.auth.core.data.entity.SignInResponse
import com.android.arch.auth.core.data.entity.SocialNetworkType.FACEBOOK
import com.android.arch.auth.core.data.network.NetworkSignInCallBack
import com.android.arch.auth.core.data.network.NetworkSignInService
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

/**
 * Created by alexk on 12/19/18.
 * Project android-auth-pack
 */
class FacebookSignInService : NetworkSignInService() {

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
                signInAndFetchCallback
                    ?.let { fetchProfile(accessToken) }
                    ?: postResult(SignInResponse(token = accessToken.token))
            }

            override fun onError(exception: FacebookException) = handleSignInError(exception)

            override fun onCancel() =
                handleSignInError(FacebookOperationCanceledException("canceled"))

        })
    }

    /**
     * Should be called in Activity.onDestroy method
     */
    override fun onDestroy() {
        super.onDestroy()
        loginManager.unregisterCallback(callbackManager)
        signInAndFetchCallback = null
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

    override fun getErrorType(exception: Exception?): AuthError? = exception?.let {
        when (it) {
            is FacebookOperationCanceledException -> CanceledAuthError()
            else -> ServiceAuthError("Facebook: SignIN failed. ${it.message}", it)
        }
    }

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
    fun signInAndFetchProfile(callback: NetworkSignInCallBack) {
        activity?.apply {
            signInAndFetchCallback = callback
            signIn(this)
        } ?: Log.w("signIn", "not attached. activity = null")
    }

    private fun handleSignInError(e: Exception) {
        val response = SignInResponse(error = getErrorType(e))
        signInAndFetchCallback
            ?.let { postSignInAndFetchResult(response) }
            ?: postResult(response)
    }

    private fun postSignInAndFetchResult(data: SignInResponse) {
        signInAndFetchCallback
            ?.let { callback -> callback(data) }
            ?: Log.w("postResult", "Wrong state. signInAndFetchCallback = null")
    }

    private fun fetchProfile(accessToken: AccessToken?) {
        accessToken?.let {
            GraphRequest.newMeRequest(accessToken) { _, response ->
                postSignInAndFetchResult(response.toFacebookSignInResponse(accessToken.token))
            }.apply {
                parameters = Bundle().apply { putString(PARAMS_KEY, PERMISSIONS) }
                executeAsync()
            }
        }
            ?: handleSignInError(FacebookAuthorizationException("Failed fetch profile. Token undefined"))
    }

    private fun GraphResponse.toFacebookSignInResponse(token: String): SignInResponse {
        val profile = try {
            jsonObject?.let {
                AuthUserProfile(
                    id = it.getString(ID),
                    name = it.getString(NAME),
                    email = it.getString(EMAIL),
                    firstName = it.getString(FIRST_NAME),
                    lastName = it.getString(LAST_NAME),
                    picture = it.getJSONObject(PICTURE)?.getJSONObject(DATA)?.getString(URL)
                )
            }
        } catch (e: Exception) {
            Log.e("SignInResponse", "Failed wrapping response", e)
            null
        }

        return SignInResponse(
            token = token,
            profile = profile,
            error = getErrorType(error?.exception)
        )
    }

    private var signInAndFetchCallback: NetworkSignInCallBack? = null

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
        private const val PERMISSIONS =
            "$ID,$NAME,$FIRST_NAME,$LAST_NAME,$EMAIL,$PICTURE.width(200)"

        private val READ_PERMISSIONS = listOf("email", "public_profile")

    }
}