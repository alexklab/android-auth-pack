package com.android.arch.auth.core.data.repository

import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.android.arch.auth.core.common.extensions.postError
import com.android.arch.auth.core.common.extensions.postEvent
import com.android.arch.auth.core.data.entity.*
import com.android.arch.auth.core.data.entity.AuthRequestStatus.*
import com.android.arch.auth.core.data.entity.AuthResponseErrorType.*
import com.android.arch.auth.core.data.network.NetworkSignInService

/**
 * Created by alexk on 12/20/18.
 * Project android-auth-pack
 */
class SingleAuthRepository<UserProfileDataType>(
    private val factory: Factory<UserProfileDataType, AuthUserProfile>
) : SocialNetworkAuthRepository<UserProfileDataType>, LifecycleObserver {

    private var service: NetworkSignInService? = null

    interface Factory<UserProfileDataType, AuthResponseType> {
        fun create(user: AuthResponseType): UserProfileDataType
    }

    /**
     * Should be called on Activity.onCreate
     */
    fun onCreate(activity: ComponentActivity, service: NetworkSignInService) {
        this.service = service
        service.onCreate(activity)

        activity.lifecycle.apply {
            removeObserver(this@SingleAuthRepository)
            addObserver(this@SingleAuthRepository)
        }
    }

    /**
     * Should be called in Activity.onDestroy method
     */
    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        service = null
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        service?.onActivityResult(requestCode, resultCode, data)
            ?: Log.e("onActivityResult", "Wrong state. Service = null")
    }

    override fun signInWithSocialNetwork(
        socialNetwork: SocialNetworkType,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ): Unit = with(response) {
        service?.signIn { (account, exception, errType) ->
            account
                ?.let { postEvent(AuthResponse(SUCCESS, data = factory.create(it))) }
                ?: postError(errType ?: AUTH_CANCELED, exception?.message)
        } ?: postError(AUTH_CANCELED, "SignIn: Wrong state. Service = null")
    }

    override fun signOut() {
        service?.signOut()
            ?: Log.e("signOut", "Wrong state. Service = null")
    }
}

