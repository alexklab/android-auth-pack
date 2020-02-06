package com.android.arch.auth.core.data.network

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import com.android.arch.auth.core.data.entity.AuthError
import com.android.arch.auth.core.data.entity.SignInResponse
import com.android.arch.auth.core.data.entity.SocialNetworkType

abstract class NetworkSignInService : OnActivityCreatedListener() {

    abstract val socialNetworkType: SocialNetworkType
    abstract fun signIn(activity: Activity)
    abstract fun signOut()
    abstract fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    open fun getErrorType(exception: Exception?): AuthError? =
        AuthError.ServiceAuthError(exception?.message ?: "Auth Error", exception)

    private val listeners = hashSetOf<SignInServiceListener>()

    fun addListener(listener: SignInServiceListener) {
        listeners.add(listener)
        Log.d("NetworkService.$socialNetworkType", "addListener: $listener")
    }

    fun removeListener(listener: SignInServiceListener) {
        listeners.remove(listener)
        Log.d("NetworkService.$socialNetworkType", "removeListener: $listener")
    }

    fun signIn() {
        activity?.apply {
            signIn(this)
        } ?: Log.w(
            "NetworkService.$socialNetworkType",
            "Failed SignIn: possibly not attached yet, activity = null"
        )
    }

    override fun onCreate(activity: ComponentActivity) {
        super.onCreate(activity)
        Log.d("NetworkService.$socialNetworkType", "onCreate: activity=$activity")
    }

    override fun onDestroy() {
        Log.d("NetworkService.$socialNetworkType", "onDestroy: activity=$activity")
        super.onDestroy()
    }

    protected fun postSignInResponse(response: SignInResponse) {
        if (listeners.isEmpty()) {
            Log.w(
                "NetworkService.$socialNetworkType",
                "Skip postSignInResponse: listener not found"
            )
        } else {
            listeners.forEach { it.onSignInResponse(socialNetworkType, response) }
        }
    }
}