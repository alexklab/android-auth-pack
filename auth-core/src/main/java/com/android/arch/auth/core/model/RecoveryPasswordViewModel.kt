package com.android.arch.auth.core.model

import com.android.arch.auth.core.common.FieldValidator
import com.android.arch.auth.core.domain.auth.RecoveryPasswordUseCase
import com.android.arch.auth.core.data.entity.AuthResponseErrorType.EMPTY_FIELD_EMAIL
import com.android.arch.auth.core.data.entity.AuthResponseErrorType.MALFORMED_EMAIL

class RecoveryPasswordViewModel<UserProfileDataType>(
        private val emailValidator: FieldValidator,
        private val recoveryPasswordUseCase: RecoveryPasswordUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>() {

    fun sendRecoveryPasswordRequest(email: String) = when {
        email.isEmpty() -> setError(EMPTY_FIELD_EMAIL)
        !emailValidator.validate(email) -> setError(MALFORMED_EMAIL)
        else -> launchAuthTask { recoveryPasswordUseCase(email, it) }
    }
}