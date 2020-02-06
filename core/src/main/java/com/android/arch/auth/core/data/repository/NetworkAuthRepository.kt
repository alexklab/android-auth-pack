package com.android.arch.auth.core.data.repository

import android.content.Intent
import androidx.activity.ComponentActivity
import com.android.arch.auth.core.data.entity.SocialNetworkType
import com.android.arch.auth.core.data.network.NetworkSignInService
import com.android.arch.auth.core.data.network.SignInServiceListener

abstract class NetworkAuthRepository<UserProfileDataType>(
    vararg services: NetworkSignInService
) : BaseAuthRepositoryImpl<UserProfileDataType>(), SignInServiceListener {

    private val signInServiceRegister: Map<SocialNetworkType, NetworkSignInService> =
        services.associateBy(NetworkSignInService::socialNetworkType)

    /**
     * Should be called on Activity.onCreate
     */
    override fun onCreate(activity: ComponentActivity) {
        super.onCreate(activity)
        signInServiceRegister.values.forEach {
            it.addListener(this@NetworkAuthRepository)
            it.onCreate(activity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        signInServiceRegister.values.forEach {
            it.removeListener(this@NetworkAuthRepository)
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        signInServiceRegister.values.forEach {
            it.onActivityResult(requestCode, resultCode, data)
        }
    }

    protected fun allServicesSignOut() {
        signInServiceRegister.values.forEach { it.signOut() }
    }

    protected fun getService(type: SocialNetworkType): NetworkSignInService? =
        signInServiceRegister[type]
}