package com.android.arch.auth.core.data.repository

import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleObserver
import com.android.arch.auth.core.data.entity.AuthError.CanceledAuthError
import com.android.arch.auth.core.data.entity.AuthRequestStatus.SUCCESS
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.AuthUserProfile
import com.android.arch.auth.core.data.entity.SignInResponse
import com.android.arch.auth.core.data.entity.SocialNetworkType
import com.android.arch.auth.core.data.network.NetworkSignInService
import com.android.arch.auth.core.data.network.SignInServiceListener

/**
 * Created by alexk on 12/20/18.
 * Project android-auth-pack
 */
open class SingleAuthRepository<UserProfileDataType>(
    private val transform: (AuthUserProfile) -> UserProfileDataType
) : BaseAuthRepositoryImpl<UserProfileDataType>(),
    SocialNetworkAuthRepository<UserProfileDataType>, LifecycleObserver {

    private var service: NetworkSignInService? = null

    private val signInServiceListener = object : SignInServiceListener {
        override fun onSignInResponse(socialNetwork: SocialNetworkType, response: SignInResponse) {
            if (response.profile != null) {
                postAuthResponse(AuthResponse(SUCCESS, data = transform(response.profile)))
            } else {
                postAuthError(response.error ?: CanceledAuthError())
            }
        }
    }

    /**
     * Should be called on Activity.onCreate
     */
    fun onCreate(activity: ComponentActivity, service: NetworkSignInService) {
        super.onCreate(activity)
        this.service = service
        service.addListener(signInServiceListener)
        service.onCreate(activity)
    }

    /**
     * Should be called in Activity.onDestroy method
     */
    override fun onDestroy() {
        super.onDestroy()
        service?.removeListener(signInServiceListener)
        service = null
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        service?.onActivityResult(requestCode, resultCode, data)
            ?: Log.e("onActivityResult", "Wrong state. Service = null")
    }

    override fun signInWithSocialNetwork(socialNetwork: SocialNetworkType) {
        service
            ?.signIn()
            ?: postAuthError(CanceledAuthError())
    }

    override fun signOut() {
        service?.signOut()
            ?: Log.e("signOut", "Wrong state. Service = null")
    }
}

