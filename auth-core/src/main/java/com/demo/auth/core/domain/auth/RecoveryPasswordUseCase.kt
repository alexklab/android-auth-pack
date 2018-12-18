package com.demo.auth.core.domain.auth

import androidx.lifecycle.MutableLiveData
import com.demo.auth.core.entity.AuthResponse
import com.demo.auth.core.entity.Event
import com.demo.auth.core.repos.AuthRepository

class RecoveryPasswordUseCase<UserProfileDataType>(private val authRepository: AuthRepository<UserProfileDataType>) {

    operator fun invoke(email: String, response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>): Unit =
            authRepository.recoverPassword(email, response)

}