package com.android.arch.auth.core.domain.auth

import com.android.arch.auth.core.data.repository.EmailAuthRepository

class SignUpUseCase<UserProfileDataType>(private val useProfileRepository: EmailAuthRepository<UserProfileDataType>) {

    operator fun invoke(login: String, email: String, password: String): Unit =
        useProfileRepository.signUp(login, email, password)

}