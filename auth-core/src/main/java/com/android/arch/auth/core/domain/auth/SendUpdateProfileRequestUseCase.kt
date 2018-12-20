package com.android.arch.auth.core.domain.auth

import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.Event
import com.android.arch.auth.core.data.repository.AuthRepository

class SendUpdateProfileRequestUseCase<UserProfileDataType>(private val useProfileRepository: AuthRepository<UserProfileDataType>) {

    operator fun invoke(uid: String, login: String, email: String, response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>): Unit =
            useProfileRepository.sendUpdateProfileRequest(uid, login, email, response)

}