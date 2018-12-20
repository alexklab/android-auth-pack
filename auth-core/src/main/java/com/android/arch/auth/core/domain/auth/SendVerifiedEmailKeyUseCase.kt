package com.android.arch.auth.core.domain.auth

import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.Event
import com.android.arch.auth.core.data.repository.AuthRepository

class SendVerifiedEmailKeyUseCase<UserProfileDataType>(private val useProfileRepository: AuthRepository<UserProfileDataType>) {

    operator fun invoke(verifyKey: String, response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>): Unit =
            useProfileRepository.sendVerifiedEmailKeyUseCase(verifyKey, response)

}