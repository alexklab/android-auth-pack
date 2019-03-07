package com.android.arch.auth.core.model

import com.android.arch.auth.core.common.FieldValidator
import com.android.arch.auth.core.data.entity.AuthResponseError.*
import com.android.arch.auth.core.domain.auth.RecoveryPasswordUseCase

class RecoveryPasswordViewModel<UserProfileDataType>(
        private val emailValidator: FieldValidator,
        private val recoveryPasswordUseCase: RecoveryPasswordUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>() {

    fun sendRecoveryPasswordRequest(email: String) = when {
        email.isEmpty() -> setError(EmailRequired)
        !emailValidator.validate(email) -> setError(MalformedEmail)
        else -> launchAuthTask { recoveryPasswordUseCase(email, it) }
    }
}