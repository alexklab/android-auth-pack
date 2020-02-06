package com.android.arch.auth.core.model

import com.android.arch.auth.core.common.FieldValidator
import com.android.arch.auth.core.data.entity.AuthError.EmailRequiredAuthError
import com.android.arch.auth.core.data.entity.AuthError.MalformedEmailAuthError
import com.android.arch.auth.core.domain.auth.AuthResponseListenerUseCase
import com.android.arch.auth.core.domain.auth.RecoveryPasswordUseCase

class RecoveryPasswordViewModel<UserProfileDataType>(
    private val emailValidator: FieldValidator,
    authResponseListenerUseCase: AuthResponseListenerUseCase<UserProfileDataType>,
    private val recoveryPasswordUseCase: RecoveryPasswordUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>(authResponseListenerUseCase) {

    fun sendRecoveryPasswordRequest(email: String) = when {
        email.isEmpty() -> setError(EmailRequiredAuthError())
        !emailValidator.validate(email) -> setError(MalformedEmailAuthError())
        else -> launchAsyncRequest { recoveryPasswordUseCase(email) }
    }
}