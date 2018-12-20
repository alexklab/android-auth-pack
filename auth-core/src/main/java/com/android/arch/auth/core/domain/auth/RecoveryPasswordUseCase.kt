package com.android.arch.auth.core.domain.auth

import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.Event
import com.android.arch.auth.core.data.repository.EmailAuthRepository

class RecoveryPasswordUseCase<UserProfileDataType>(private val authRepository: EmailAuthRepository<UserProfileDataType>) {

    operator fun invoke(email: String, response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>): Unit =
            authRepository.recoverPassword(email, response)

}