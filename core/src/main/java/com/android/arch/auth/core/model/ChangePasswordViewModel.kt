package com.android.arch.auth.core.model

import com.android.arch.auth.core.common.FieldValidator
import com.android.arch.auth.core.data.entity.AuthResponseError.*
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
        oldPassword.isEmpty() -> setError(OldPasswordRequired)
        newPassword.isEmpty() -> setError(PasswordRequired)
        !passwordValidator.validate(newPassword) -> setError(WeakPassword)
        newConfirmPassword.isEmpty() -> setError(ConfirmPasswordRequired)
        newPassword != newConfirmPassword -> setError(NotMatchedConfirmPassword)
        else -> launchAuthTask {
            getProfileUidUseCase()?.let { uid ->
                changePasswordUseCase(uid, oldPassword, newPassword, it)
            }
        }
    }
}