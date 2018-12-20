package com.android.arch.auth.core.domain.auth

import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.Event
import com.android.arch.auth.core.data.repository.EmailAuthRepository

class SendVerifiedEmailKeyUseCase<UserProfileDataType>(private val useProfileRepository: EmailAuthRepository<UserProfileDataType>) {

    operator fun invoke(verifyKey: String, response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>): Unit =
            useProfileRepository.sendVerifiedEmailKeyUseCase(verifyKey, response)

}