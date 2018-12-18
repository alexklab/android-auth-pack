package com.demo.auth.core.model

import com.demo.auth.core.common.FieldValidator
import com.demo.auth.core.domain.auth.RecoveryPasswordUseCase
import com.demo.auth.core.entity.AuthResponseErrorType.EMPTY_EMAIL
import com.demo.auth.core.entity.AuthResponseErrorType.INVALID_EMAIL

class RecoveryPasswordViewModel<UserProfileDataType>(
        private val emailValidator: FieldValidator,
        private val recoveryPasswordUseCase: RecoveryPasswordUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>() {

    fun sendRecoveryPasswordRequest(email: String) = when {
        email.isEmpty() -> postError(EMPTY_EMAIL)
        !emailValidator.validate(email) -> postError(INVALID_EMAIL)
        else -> launchAuthTask { recoveryPasswordUseCase(email, it) }
    }
}