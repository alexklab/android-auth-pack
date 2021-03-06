package com.android.arch.auth.core.domain.auth

import com.android.arch.auth.core.data.entity.SocialNetworkType
import com.android.arch.auth.core.data.repository.SocialNetworkAuthRepository

/**
 * Created by alexk on 10/29/18.
 * Project android-auth-pack
 */
class SignInWithSocialNetworkUseCase<UserProfileDataType>(private val repository: SocialNetworkAuthRepository<UserProfileDataType>) {

    operator fun invoke(socialNetwork: SocialNetworkType) {
        repository.signInWithSocialNetwork(socialNetwork)
    }
}