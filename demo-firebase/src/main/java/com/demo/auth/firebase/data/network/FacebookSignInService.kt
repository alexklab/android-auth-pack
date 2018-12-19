package com.demo.auth.firebase.data.network

import android.app.Activity
import android.content.Intent
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider

/**
 * Created by alexk on 12/19/18.
 * Project android-auth-pack
 */
class FacebookSignInService : NetworkSignInService() {

    private val callbackManager: CallbackManager by lazy { CallbackManager.Factory.create() }
    private val loginManager: LoginManager = LoginManager.getInstance()

    /**
     * Should be called in Activity.onCreate method
     */
    override fun onCreate(activity: Activity) {
        super.onCreate(activity)
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onCancel() = postResult()
            override fun onError(e: FacebookException) = postResult(exception = e)
            override fun onSuccess(result: LoginResult) = postResult(result.accessToken.toAuthCredential())
        })
    }

    /**
     * Should be called in Activity.onDestroy method
     */
    override fun onDestroy() {
        super.onDestroy()
        loginManager.unregisterCallback(callbackManager)
    }

    override fun signIn(activity: Activity) {
        loginManager.logInWithReadPermissions(activity, listOf("email", "public_profile"))
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun AccessToken.toAuthCredential() = FacebookAuthProvider.getCredential(token)
}