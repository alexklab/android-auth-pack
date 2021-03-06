package com.android.arch.auth.core.model

import com.android.arch.auth.core.common.EmailFieldValidator
import com.android.arch.auth.core.data.entity.AuthError.EmailRequiredAuthError
import com.android.arch.auth.core.data.entity.AuthError.MalformedEmailAuthError
import com.android.arch.auth.core.domain.auth.AuthResponseListenerUseCase
import com.android.arch.auth.core.domain.auth.RecoveryPasswordUseCase

open class RecoveryPasswordViewModel<UserProfileDataType>(
    private val emailValidator: EmailFieldValidator,
    authResponseListenerUseCase: AuthResponseListenerUseCase<UserProfileDataType>,
    private val recoveryPasswordUseCase: RecoveryPasswordUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>(authResponseListenerUseCase) {

    fun sendRecoveryPasswordRequest(email: String) = when {
        email.isEmpty() -> setError(EmailRequiredAuthError())
        !emailValidator.validate(email) -> setError(MalformedEmailAuthError())
        else -> launchAsyncRequest { recoveryPasswordUseCase(email) }
    }
}