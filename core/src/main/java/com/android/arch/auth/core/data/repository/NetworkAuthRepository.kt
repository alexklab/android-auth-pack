package com.android.arch.auth.core.data.repository

import android.content.Intent
import androidx.activity.ComponentActivity
import com.android.arch.auth.core.data.entity.SocialNetworkType
import com.android.arch.auth.core.data.network.NetworkSignInService
import com.android.arch.auth.core.data.network.OnActivityCreatedListener

abstract class NetworkAuthRepository : OnActivityCreatedListener() {

    private val signInServiceRegister = hashMapOf<SocialNetworkType, NetworkSignInService<*>>()

    override fun onDestroy() {
        super.onDestroy()
        signInServiceRegister.clear()
    }

    /**
     * Should be called on Activity.onCreate
     */
    fun onCreate(activity: ComponentActivity, vararg services: NetworkSignInService<*>) {
        super.onCreate(activity)
        services.forEach {
            it.onCreate(activity)
            signInServiceRegister[it.socialNetworkType] = it
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