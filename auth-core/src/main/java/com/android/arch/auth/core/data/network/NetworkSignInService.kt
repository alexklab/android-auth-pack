package com.android.arch.auth.core.data.network

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.android.arch.auth.core.data.entity.SocialNetworkType

abstract class NetworkSignInService<ResponseDataType> : LifecycleObserver {

    protected var activity: Activity? = null
    private var signInCallback: NetworkSignInCallBack<ResponseDataType>? = null

    abstract val socialNetworkType: SocialNetworkType
    abstract fun signIn(activity: Activity)
    abstract fun signOut()
    abstract fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    protected abstract fun getParamsBundle(data: ResponseDataType): ParamsBundle

    fun signIn(callback: NetworkSignInCallBack<ResponseDataType>) {
        activity?.apply {
            signInCallback = callback
            signIn(this)
        } ?: Log.w("signIn", "not attached. activity = null")
    }

    /**
     * Should be called in Activity.onCreate method
     */
    open fun onCreate(activity: ComponentActivity) {
        this.activity = activity

        activity.lifecycle.apply {
            removeObserver(this@NetworkSignInService)
            addObserver(this@NetworkSignInService)
        }
    }

    /**
     * Should be called in Activity.onDestroy method
     */
    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun onDestroy() {
        activity = null
        signInCallback = null
    }

    protected fun postResult(data: ResponseDataType? = null, exception: Exception? = null) {
        signInCallback
            ?.let { callback -> callback(data, data?.let { getParamsBundle(it) }, exception) }
            ?: Log.w("postResult", "Wrong state. signInCallback = null")
    }
}