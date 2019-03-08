package com.android.arch.auth.core.model

import com.android.arch.auth.core.common.FieldValidator
import com.android.arch.auth.core.data.entity.AuthError.*
import com.android.arch.auth.core.domain.auth.ChangePasswordUseCase
import com.android.arch.auth.core.domain.profile.GetProfileUidUseCase

/**
 * Created by alexk on 11/21/18.
 * Project android-auth-pack
 */
class ChangePasswordViewModel<UserProfileDataType>(
        private val passwordValidator: FieldValidator,
        private val changePasswordUseCase: ChangePasswordUseCase<UserProfileDataType>,
        private val getProfileUidUseCase: GetProfileUidUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>() {

    fun changePassword(oldPassword: String, newPassword: String, newConfirmPassword: String): Unit = when {
        oldPassword.isEmpty() -> setError(OldPasswordRequiredAuthError)
        newPassword.isEmpty() -> setError(PasswordRequiredAuthError)
        !passwordValidator.validate(newPassword) -> setError(WeakPasswordAuthError)
        newConfirmPassword.isEmpty() -> setError(ConfirmPasswordRequiredAuthError)
        newPassword != newConfirmPassword -> setError(NotMatchedConfirmPasswordAuthError)
        else -> launchAuthTask {
            getProfileUidUseCase()?.let { uid ->
                changePasswordUseCase(uid, oldPassword, newPassword, it)
            }
        }
    }
}