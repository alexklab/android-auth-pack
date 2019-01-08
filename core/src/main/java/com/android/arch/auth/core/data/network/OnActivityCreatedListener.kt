package com.android.arch.auth.core.data.network

import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

abstract class OnActivityCreatedListener : LifecycleObserver {

    protected var activity: ComponentActivity? = null

    /**
     * Should be called in Activity.onCreate method
     */
    open fun onCreate(activity: ComponentActivity) {
        onDestroy() // case when activity recreated
        this.activity = activity
        activity.lifecycle.apply {
            removeObserver(this@OnActivityCreatedListener)
            addObserver(this@OnActivityCreatedListener)
        }
    }

    /**
     * Should be called in Activity.onDestroy method
     */
    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun onDestroy() {
        activity
            ?.lifecycle
            ?.apply { removeObserver(this@OnActivityCreatedListener) }
        activity = null
    }

}