package com.demo.auth.core.model

import com.demo.auth.core.common.FieldValidator
import com.demo.auth.core.domain.auth.SendUpdateProfileRequestUseCase
import com.demo.auth.core.domain.profile.GetProfileUidUseCase
import com.demo.auth.core.entity.AuthResponseErrorType.*

class EditProfileViewModel<UserProfileDataType>(
        private val emailValidator: FieldValidator,
        private val loginValidator: FieldValidator,
        private val sendUpdateProfileRequestUseCase: SendUpdateProfileRequestUseCase<UserProfileDataType>,
        private val getProfileUidUseCase: GetProfileUidUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>() {

    fun updateProfile(login: String = "", email: String = "") = when {
        login.isEmpty() -> setError(EMPTY_LOGIN)
        !loginValidator.validate(login) -> setError(INVALID_LOGIN)
        email.isEmpty() -> setError(EMPTY_EMAIL)
        !emailValidator.validate(email) -> setError(INVALID_EMAIL)
        else -> launchAuthTask {
            getProfileUidUseCase()?.let { uid ->
                sendUpdateProfileRequestUseCase(uid, login, email, it)
            }
        }
    }
}