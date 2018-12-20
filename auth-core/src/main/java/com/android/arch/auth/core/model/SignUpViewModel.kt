package com.android.arch.auth.core.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import com.android.arch.auth.core.common.FieldValidator
import com.android.arch.auth.core.common.extensions.applyOnSuccess
import com.android.arch.auth.core.domain.auth.SignUpUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.AuthResponseErrorType.*
import com.android.arch.auth.core.data.entity.Event

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
        login.isEmpty() -> setError(EMPTY_LOGIN)
        !loginValidator.validate(login) -> setError(INVALID_LOGIN)
        email.isEmpty() -> setError(EMPTY_EMAIL)
        !emailValidator.validate(email) -> setError(INVALID_EMAIL)
        password.isEmpty() -> setError(EMPTY_PASSWORD)
        !passwordValidator.validate(password) -> setError(INVALID_PASSWORD)
        confirmPassword.isEmpty() -> setError(EMPTY_CONFIRM_PASSWORD)
        confirmPassword != password -> setError(INVALID_CONFIRM_PASSWORD)
        !isEnabledTermsOfUse -> setError(DISABLED_TERMS_OF_USE)
        else -> launchAuthTask { signUpUseCase(login, email, password, it) }
    }
}