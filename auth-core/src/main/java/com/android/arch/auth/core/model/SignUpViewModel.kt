package com.android.arch.auth.core.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import com.android.arch.auth.core.common.FieldValidator
import com.android.arch.auth.core.common.extensions.applyOnSuccess
import com.android.arch.auth.core.domain.auth.SignUpUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase
import com.android.arch.auth.core.entity.AuthResponse
import com.android.arch.auth.core.entity.AuthResponseErrorType.*
import com.android.arch.auth.core.entity.Event

class SignUpViewModel<UserProfileDataType>(
        private val emailValidator: FieldValidator,
        private val loginValidator: FieldValidator,
        private val passwordValidator: FieldValidator,
        private val signUpUseCase: SignUpUseCase<UserProfileDataType>,
        private val updateProfileUseCase: UpdateProfileUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>() {

    override val response: LiveData<Event<AuthResponse<UserProfileDataType>>> = map(getRawResponseData()) {
        it.applyOnSuccess(updateProfileUseCase::invoke)
    }

    fun signUp(login: String, email: String, password: String, confirmPassword: String, isEnabledTermsOfUse: Boolean) = when {
        login.isEmpty() -> postError(EMPTY_LOGIN)
        !loginValidator.validate(login) -> postError(INVALID_LOGIN)
        email.isEmpty() -> postError(EMPTY_EMAIL)
        !emailValidator.validate(email) -> postError(INVALID_EMAIL)
        password.isEmpty() -> postError(EMPTY_PASSWORD)
        !passwordValidator.validate(password) -> postError(INVALID_PASSWORD)
        confirmPassword.isEmpty() -> postError(EMPTY_CONFIRM_PASSWORD)
        confirmPassword != password -> postError(INVALID_CONFIRM_PASSWORD)
        !isEnabledTermsOfUse -> postError(DISABLED_TERMS_OF_USE)
        else -> launchAuthTask { signUpUseCase(login, email, password, it) }
    }
}