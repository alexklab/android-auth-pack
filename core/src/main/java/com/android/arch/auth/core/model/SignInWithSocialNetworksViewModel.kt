package com.android.arch.auth.core.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import com.android.arch.auth.core.common.extensions.applyOnSuccess
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.Event
import com.android.arch.auth.core.data.entity.SocialNetworkType
import com.android.arch.auth.core.domain.auth.AuthResponseListenerUseCase
import com.android.arch.auth.core.domain.auth.SignInWithSocialNetworkUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase

interface NetworkSignInViewModel<UserProfileDataType> {
    val response: LiveData<Event<AuthResponse<UserProfileDataType>>>
    fun signInWithSocialNetwork(socialNetwork: SocialNetworkType)
}

open class SignInWithSocialNetworksViewModel<UserProfileDataType>(
    authResponseListenerUseCase: AuthResponseListenerUseCase<UserProfileDataType>,
    private val signInWithSocialNetworkUseCase: SignInWithSocialNetworkUseCase<UserProfileDataType>,
    private val updateProfileUseCase: UpdateProfileUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>(authResponseListenerUseCase),
    NetworkSignInViewModel<UserProfileDataType> {

    override val response: LiveData<Event<AuthResponse<UserProfileDataType>>> =
        map(getRawResponseData()) {
            it.applyOnSuccess { data ->
                launchAsync { updateProfileUseCase(data) }
            }
        }

    override fun signInWithSocialNetwork(socialNetwork: SocialNetworkType) {
        launchAsyncRequest { signInWithSocialNetworkUseCase(socialNetwork) }
    }

}