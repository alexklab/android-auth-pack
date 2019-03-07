package com.android.arch.auth.core.model

import com.android.arch.auth.core.common.FieldValidator
import com.android.arch.auth.core.data.entity.AuthResponseError.*
import com.android.arch.auth.core.domain.auth.SendUpdateProfileRequestUseCase
import com.android.arch.auth.core.domain.profile.GetProfileUidUseCase

class EditProfileViewModel<UserProfileDataType>(
        private val emailValidator: FieldValidator,
        private val loginValidator: FieldValidator,
        private val sendUpdateProfileRequestUseCase: SendUpdateProfileRequestUseCase<UserProfileDataType>,
        private val getProfileUidUseCase: GetProfileUidUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>() {

    fun updateProfile(login: String = "", email: String = "") = when {
        login.isEmpty() -> setError(LoginRequired)
        !loginValidator.validate(login) -> setError(MalformedLogin)
        email.isEmpty() -> setError(EmailRequired)
        !emailValidator.validate(email) -> setError(MalformedEmail)
        else -> launchAuthTask {
            getProfileUidUseCase()?.let { uid ->
                sendUpdateProfileRequestUseCase(uid, login, email, it)
            }
        }
    }
}