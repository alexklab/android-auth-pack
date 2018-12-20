package com.demo.auth.firebase.data.network

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import com.android.arch.auth.core.data.entity.SocialNetworkType.FACEBOOK
import com.android.arch.auth.core.data.network.NetworkSignInService
import com.android.arch.auth.core.data.network.ParamsBundle
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
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
            override fun onCancel() = postResult()
            override fun onError(e: FacebookException) = postResult(exception = e)
            override fun onSuccess(result: LoginResult) = postResult(result.accessToken)
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

    override fun signOut() {
        loginManager.logOut()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun getParamsBundle(data: AccessToken) = ParamsBundle(data.token)
}