package com.android.arch.auth.core.domain.auth

import com.android.arch.auth.core.data.repository.AuthRepositoryListener
import com.android.arch.auth.core.data.repository.BaseAuthRepository

/**
 * Created by alexk on 10/29/18.
 * Project android-auth-pack
 */
class AuthResponseListenerUseCase<UserProfileDataType>(private val repository: BaseAuthRepository<UserProfileDataType>) {

    fun addListener(listener: AuthRepositoryListener<UserProfileDataType>) {
        repository.addListener(listener)
    }

    fun removeListener(listener: AuthRepositoryListener<UserProfileDataType>) {
        repository.removeListener(listener)
    }
}