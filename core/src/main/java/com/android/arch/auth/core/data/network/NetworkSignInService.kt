package com.android.arch.auth.core.data.network

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.android.arch.auth.core.data.entity.AuthError
import com.android.arch.auth.core.data.entity.SignInResponse
import com.android.arch.auth.core.data.entity.SocialNetworkType

abstract class NetworkSignInService : OnActivityCreatedListener() {

    abstract val socialNetworkType: SocialNetworkType
    abstract fun signIn(activity: Activity)
    abstract fun signOut()
    abstract fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    open fun getErrorType(exception: Exception?): AuthError? = null

    private val listeners = hashSetOf<SignInServiceListener>()

    fun addListener(listener: SignInServiceListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: SignInServiceListener) {
        listeners.remove(listener)
    }

    fun signIn() {
        activity?.apply {
            signIn(this)
        } ?: Log.w("signIn", "not attached. activity = null")
    }

    protected fun postSignInResponse(response: SignInResponse) {
        if (listeners.isEmpty()) {
            Log.w("$this\$postResult", "Wrong state. Not found SignInResponseListener")
        } else {
            listeners.forEach { it.onSignInResponse(socialNetworkType, response) }
        }
    }
}