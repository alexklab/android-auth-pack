package com.android.arch.auth.core.domain.auth

import com.android.arch.auth.core.data.repository.EmailAuthRepository

class RecoveryPasswordUseCase<UserProfileDataType>(private val authRepository: EmailAuthRepository<UserProfileDataType>) {

    operator fun invoke(email: String): Unit = authRepository.recoverPassword(email)

}