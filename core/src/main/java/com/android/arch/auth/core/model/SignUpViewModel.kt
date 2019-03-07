package com.android.arch.auth.core.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import com.android.arch.auth.core.common.FieldValidator
import com.android.arch.auth.core.common.extensions.applyOnSuccess
import com.android.arch.auth.core.domain.auth.SignUpUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.AuthResponseError.*
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
        login.isEmpty() -> setError(LoginRequired)
        !loginValidator.validate(login) -> setError(MalformedLogin)
        email.isEmpty() -> setError(EmailRequired)
        !emailValidator.validate(email) -> setError(MalformedEmail)
        password.isEmpty() -> setError(PasswordRequired)
        !passwordValidator.validate(password) -> setError(WeakPassword)
        confirmPassword.isEmpty() -> setError(ConfirmPasswordRequired)
        confirmPassword != password -> setError(NotMatchedConfirmPassword)
        !isEnabledTermsOfUse -> setError(EnableTermsOfUseRequired)
        else -> launchAuthTask { signUpUseCase(login, email, password, it) }
    }
}