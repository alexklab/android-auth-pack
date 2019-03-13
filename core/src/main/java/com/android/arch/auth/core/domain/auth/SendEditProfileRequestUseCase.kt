package com.android.arch.auth.core.domain.auth

import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.EditProfileRequest
import com.android.arch.auth.core.data.entity.Event
import com.android.arch.auth.core.data.repository.EmailAuthRepository

class SendEditProfileRequestUseCase<UserProfileDataType>(private val useProfileRepository: EmailAuthRepository<UserProfileDataType>) {

    operator fun invoke(request: EditProfileRequest, response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>): Unit =
            useProfileRepository.editProfile(request, response)

}