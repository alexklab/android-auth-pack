package com.demo.auth.firebase.data.network

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.AuthCredential

abstract class NetworkSignInService {

    protected var activity: Activity? = null
    private var signInCallback: NetworkSignInCallBack? = null

    abstract fun signIn(activity: Activity)

    fun signIn(callback: NetworkSignInCallBack) {
        activity?.apply {
            signInCallback = callback
            signIn(this)
        } ?: Log.w("signIn", "not attached. activity = null")
    }

    /**
     * Should be called in Activity.onCreate method
     */
    open fun onCreate(activity: Activity) {
        this.activity = activity
    }

    /**
     * Should be called in Activity.onDestroy method
     */
    open fun onDestroy() {
        activity = null
        signInCallback = null
    }

    protected fun postResult(credential: AuthCredential? = null, exception: Exception? = null) {
        signInCallback
            ?.let { it(credential, exception) }
            ?: Log.w("postResult", "Wrong state. signInCallback = null")
    }
}