package com.android.arch.auth.core.domain.auth

import com.android.arch.auth.core.data.repository.EmailAuthRepository

class SendVerifiedEmailKeyUseCase<UserProfileDataType>(private val useProfileRepository: EmailAuthRepository<UserProfileDataType>) {

    operator fun invoke(verifyKey: String): Unit =
        useProfileRepository.sendVerifiedEmailKeyUseCase(verifyKey)

}