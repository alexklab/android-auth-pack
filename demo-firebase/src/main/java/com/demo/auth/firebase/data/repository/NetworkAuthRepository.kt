package com.demo.auth.firebase.data.repository

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.demo.auth.core.entity.SocialNetworkType
import com.demo.auth.firebase.data.network.NetworkSignInService

abstract class NetworkAuthRepository : LifecycleObserver {

    private val signInServiceRegister = hashMapOf<SocialNetworkType, NetworkSignInService<*>>()

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        signInServiceRegister.clear()
    }

    /**
     * Should be called on Activity.onCreate
     */
    fun onCreate(activity: ComponentActivity, vararg services: NetworkSignInService<*>) {
        services.forEach {
            it.onCreate(activity)
            signInServiceRegister[it.socialNetworkType] = it
        }

        activity.lifecycle.apply {
            removeObserver(this@NetworkAuthRepository)
            addObserver(this@NetworkAuthRepository)
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        signInServiceRegister.forEach { (_, service) ->
            service.onActivityResult(requestCode, resultCode, data)
        }
    }

    protected fun allServicesSignOut() {
        signInServiceRegister.forEach { (_, service) -> service.signOut() }
    }

    protected fun getService(type: SocialNetworkType): NetworkSignInService<*>? = signInServiceRegister[type]
}