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
        oldPassword.isEmpty() -> postError(EMPTY_PASSWORD)
        newPassword.isEmpty() -> postError(EMPTY_NEW_PASSWORD)
        !passwordValidator.validate(newPassword) -> postError(INVALID_PASSWORD)
        newConfirmPassword.isEmpty() -> postError(EMPTY_CONFIRM_PASSWORD)
        newPassword != newConfirmPassword -> postError(INVALID_CONFIRM_PASSWORD)
        else -> launchAuthTask {
            getProfileUidUseCase()?.let { uid ->
                changePasswordUseCase(uid, oldPassword, newPassword, it)
            }
        }
    }
}