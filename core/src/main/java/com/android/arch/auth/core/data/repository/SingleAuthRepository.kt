package com.android.arch.auth.core.data.repository

import androidx.lifecycle.LifecycleObserver
import com.android.arch.auth.core.data.entity.AuthError.CanceledAuthError
import com.android.arch.auth.core.data.entity.AuthRequestStatus.SUCCESS
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.AuthUserProfile
import com.android.arch.auth.core.data.entity.SignInResponse
import com.android.arch.auth.core.data.entity.SocialNetworkType
import com.android.arch.auth.core.data.network.NetworkSignInService

/**
 * Created by alexk on 12/20/18.
 * Project android-auth-pack
 */
open class SingleAuthRepository<UserProfileDataType>(
    private val transform: (AuthUserProfile) -> UserProfileDataType,
    private val service: NetworkSignInService
) : NetworkAuthRepository<UserProfileDataType>(service),
    SocialNetworkAuthRepository<UserProfileDataType>, LifecycleObserver {

    override fun onSignInResponse(socialNetwork: SocialNetworkType, response: SignInResponse) {
        if (response.profile != null) {
            postAuthResponse(AuthResponse(SUCCESS, data = transform(response.profile)))
        } else {
            postAuthError(response.error ?: CanceledAuthError())
        }
    }

    override fun signInWithSocialNetwork(socialNetwork: SocialNetworkType) {
        service.signIn()
    }

    override fun signOut() {
        service.signOut()
    }
}

