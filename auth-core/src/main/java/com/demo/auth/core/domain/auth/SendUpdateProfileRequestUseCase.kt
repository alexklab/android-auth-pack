package com.demo.auth.core.domain.auth

import androidx.lifecycle.MutableLiveData
import com.demo.auth.core.entity.AuthResponse
import com.demo.auth.core.entity.Event
import com.demo.auth.core.repos.AuthRepository

class SendUpdateProfileRequestUseCase<UserProfileDataType>(private val useProfileRepository: AuthRepository<UserProfileDataType>) {

    operator fun invoke(uid: String, login: String, email: String, response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>): Unit =
            useProfileRepository.sendUpdateProfileRequest(uid, login, email, response)

}