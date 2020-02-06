package com.android.arch.auth.core.domain.auth

import com.android.arch.auth.core.data.entity.EditProfileRequest
import com.android.arch.auth.core.data.repository.EmailAuthRepository

class SendEditProfileRequestUseCase<UserProfileDataType>(private val useProfileRepository: EmailAuthRepository<UserProfileDataType>) {

    operator fun invoke(request: EditProfileRequest): Unit =
        useProfileRepository.editProfile(request)

}