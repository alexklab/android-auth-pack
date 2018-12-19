package com.demo.auth.core.model

import com.demo.auth.core.common.FieldValidator
import com.demo.auth.core.domain.auth.ChangePasswordUseCase
import com.demo.auth.core.domain.profile.GetProfileUidUseCase
import com.demo.auth.core.entity.AuthResponseErrorType.*

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
        oldPassword.isEmpty() -> setError(EMPTY_PASSWORD)
        newPassword.isEmpty() -> setError(EMPTY_NEW_PASSWORD)
        !passwordValidator.validate(newPassword) -> setError(INVALID_PASSWORD)
        newConfirmPassword.isEmpty() -> setError(EMPTY_CONFIRM_PASSWORD)
        newPassword != newConfirmPassword -> setError(INVALID_CONFIRM_PASSWORD)
        else -> launchAuthTask {
            getProfileUidUseCase()?.let { uid ->
                changePasswordUseCase(uid, oldPassword, newPassword, it)
            }
        }
    }
}