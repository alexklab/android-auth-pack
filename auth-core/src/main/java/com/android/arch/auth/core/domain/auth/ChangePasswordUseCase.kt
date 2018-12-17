package com.android.arch.auth.core.domain.auth

import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.entity.AuthResponse
import com.android.arch.auth.core.entity.Event
import com.android.arch.auth.core.repos.AuthRepository

/**
 * Created by alexk on 10/29/18.
 * Project android-auth-pack
 */
class ChangePasswordUseCase<UserProfileDataType>(private val repository: AuthRepository<UserProfileDataType>) {

    operator fun invoke(uid: String, oldPassword: String, newPassword: String, response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>): Unit =
            repository.changePassword(uid, oldPassword, newPassword, response)
}