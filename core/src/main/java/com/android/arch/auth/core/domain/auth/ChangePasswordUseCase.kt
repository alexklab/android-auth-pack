package com.android.arch.auth.core.domain.auth

import com.android.arch.auth.core.data.repository.EmailAuthRepository

/**
 * Created by alexk on 10/29/18.
 * Project android-auth-pack
 */
class ChangePasswordUseCase<UserProfileDataType>(private val repository: EmailAuthRepository<UserProfileDataType>) {

    operator fun invoke(uid: String, oldPassword: String, newPassword: String): Unit =
        repository.changePassword(uid, oldPassword, newPassword)
}