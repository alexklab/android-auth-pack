package com.demo.auth.core.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import com.demo.auth.core.common.extensions.applyOnSuccess
import com.demo.auth.core.domain.auth.SignInWithSocialNetworkUseCase
import com.demo.auth.core.domain.profile.UpdateProfileUseCase
import com.demo.auth.core.entity.AuthResponse
import com.demo.auth.core.entity.Event
import com.demo.auth.core.entity.SocialNetworkType

class SignInWithSocialNetworksViewModel<UserProfileDataType>(
        private val signInWithSocialNetworkUseCase: SignInWithSocialNetworkUseCase<UserProfileDataType>,
        private val updateProfileUseCase: UpdateProfileUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>() {

    override val response: LiveData<Event<AuthResponse<UserProfileDataType>>> = map(getRawResponseData()) {
        it.applyOnSuccess(updateProfileUseCase::invoke)
    }

    fun signInWithSocialNetwork(socialNetwork: SocialNetworkType) {
        launchAuthTask { signInWithSocialNetworkUseCase(socialNetwork, it) }
    }


}