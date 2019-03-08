package com.android.arch.auth.core.model

import com.android.arch.auth.core.common.FieldValidator
import com.android.arch.auth.core.data.entity.AuthError.*
import com.android.arch.auth.core.domain.auth.SendUpdateProfileRequestUseCase
import com.android.arch.auth.core.domain.profile.GetProfileUidUseCase

class EditProfileViewModel<UserProfileDataType>(
        private val emailValidator: FieldValidator,
        private val loginValidator: FieldValidator,
        private val sendUpdateProfileRequestUseCase: SendUpdateProfileRequestUseCase<UserProfileDataType>,
        private val getProfileUidUseCase: GetProfileUidUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>() {

    fun updateProfile(login: String = "", email: String = "") = when {
        login.isEmpty() -> setError(LoginRequiredAuthError)
        !loginValidator.validate(login) -> setError(MalformedLoginAuthError)
        email.isEmpty() -> setError(EmailRequiredAuthError)
        !emailValidator.validate(email) -> setError(MalformedEmailAuthError)
        else -> launchAuthTask {
            getProfileUidUseCase()?.let { uid ->
                sendUpdateProfileRequestUseCase(uid, login, email, it)
            }
        }
    }
}