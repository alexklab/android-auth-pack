package com.android.arch.auth.core.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import com.android.arch.auth.core.common.EmailFieldValidator
import com.android.arch.auth.core.common.LoginFieldValidator
import com.android.arch.auth.core.common.PasswordFieldValidator
import com.android.arch.auth.core.common.extensions.applyOnSuccess
import com.android.arch.auth.core.data.entity.AuthError.*
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.Event
import com.android.arch.auth.core.domain.auth.AuthResponseListenerUseCase
import com.android.arch.auth.core.domain.auth.SignUpUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase

open class SignUpViewModel<UserProfileDataType>(
    private val emailValidator: EmailFieldValidator,
    private val loginValidator: LoginFieldValidator,
    private val passwordValidator: PasswordFieldValidator,
    authResponseListenerUseCase: AuthResponseListenerUseCase<UserProfileDataType>,
    private val signUpUseCase: SignUpUseCase<UserProfileDataType>,
    private val updateProfileUseCase: UpdateProfileUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>(authResponseListenerUseCase) {

    override val response: LiveData<Event<AuthResponse<UserProfileDataType>>> =
        map(getRawResponseData()) {
            it.applyOnSuccess { data ->
                launchAsync { updateProfileUseCase(data) }
            }
        }

    fun signUp(
        login: String,
        email: String,
        password: String,
        confirmPassword: String,
        isEnabledTermsOfUse: Boolean
    ) = when {
        login.isEmpty() -> setError(LoginRequiredAuthError())
        !loginValidator.validate(login) -> setError(MalformedLoginAuthError())
        email.isEmpty() -> setError(EmailRequiredAuthError())
        !emailValidator.validate(email) -> setError(MalformedEmailAuthError())
        password.isEmpty() -> setError(PasswordRequiredAuthError())
        !passwordValidator.validate(password) -> setError(WeakPasswordAuthError())
        confirmPassword.isEmpty() -> setError(ConfirmPasswordRequiredAuthError())
        confirmPassword != password -> setError(NotMatchedConfirmPasswordAuthError())
        !isEnabledTermsOfUse -> setError(EnableTermsOfUseAuthError())
        else -> launchAsyncRequest { signUpUseCase(login, email, password) }
    }
}