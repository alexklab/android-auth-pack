package com.android.arch.auth.core.data.network

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.android.arch.auth.core.data.entity.AuthResponseError
import com.android.arch.auth.core.data.entity.SignInResponse
import com.android.arch.auth.core.data.entity.SocialNetworkType

abstract class NetworkSignInService : OnActivityCreatedListener() {

    private var signInCallback: NetworkSignInCallBack? = null

    abstract val socialNetworkType: SocialNetworkType
    abstract fun signIn(activity: Activity)
    abstract fun signOut()
    abstract fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    open fun getErrorType(exception: Exception?): AuthResponseError? = null

    fun signIn(callback: NetworkSignInCallBack) {
        activity?.apply {
            signInCallback = callback
            signIn(this)
        } ?: Log.w("signIn", "not attached. activity = null")
    }

    /**
     * Should be called in Activity.onDestroy method
     */
    override fun onDestroy() {
        super.onDestroy()
        signInCallback = null
    }

    protected fun postResult(response: SignInResponse) {
        signInCallback
            ?.let { callback -> callback(response) }
            ?: Log.w("postResult", "Wrong state. signInCallback = null")
    }
}