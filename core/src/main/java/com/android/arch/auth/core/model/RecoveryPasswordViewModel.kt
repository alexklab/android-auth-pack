package com.android.arch.auth.core.model

import com.android.arch.auth.core.common.FieldValidator
import com.android.arch.auth.core.data.entity.AuthError.EmailRequiredAuthError
import com.android.arch.auth.core.data.entity.AuthError.MalformedEmailAuthError
import com.android.arch.auth.core.domain.auth.RecoveryPasswordUseCase

class RecoveryPasswordViewModel<UserProfileDataType>(
        private val emailValidator: FieldValidator,
        private val recoveryPasswordUseCase: RecoveryPasswordUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>() {

    fun sendRecoveryPasswordRequest(email: String) = when {
        email.isEmpty() -> setError(EmailRequiredAuthError())
        !emailValidator.validate(email) -> setError(MalformedEmailAuthError())
        else -> launchAuthTask { recoveryPasswordUseCase(email, it) }
    }
}