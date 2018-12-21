package com.android.arch.auth.core.domain.auth

import com.android.arch.auth.core.data.repository.SocialNetworkAuthRepository

class NetworksSignOutUseCase<UserProfileDataType>(private val repository: SocialNetworkAuthRepository<UserProfileDataType>) {

    operator fun invoke(): Unit = repository.signOut()

}