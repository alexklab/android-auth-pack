package com.android.arch.auth.core.domain.auth

import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.entity.AuthResponse
import com.android.arch.auth.core.entity.Event
import com.android.arch.auth.core.repos.AuthRepository

class SendVerifiedEmailKeyUseCase<UserProfileDataType>(private val useProfileRepository: AuthRepository<UserProfileDataType>) {

    operator fun invoke(verifyKey: String, response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>): Unit =
            useProfileRepository.sendVerifiedEmailKeyUseCase(verifyKey, response)

}