package com.android.arch.auth.core.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import com.android.arch.auth.core.common.FieldValidator
import com.android.arch.auth.core.common.extensions.applyOnSuccess
import com.android.arch.auth.core.data.entity.AuthError.*
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.EditProfileRequest
import com.android.arch.auth.core.data.entity.Event
import com.android.arch.auth.core.data.entity.RequestParam
import com.android.arch.auth.core.domain.auth.AuthResponseListenerUseCase
import com.android.arch.auth.core.domain.auth.SendEditProfileRequestUseCase
import com.android.arch.auth.core.domain.profile.GetProfileUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase

class EditProfileViewModel<UserProfileDataType>(
    private val emailValidator: FieldValidator,
    private val loginValidator: FieldValidator,
    authResponseListenerUseCase: AuthResponseListenerUseCase<UserProfileDataType>,
    private val sendEditProfileRequestUseCase: SendEditProfileRequestUseCase<UserProfileDataType>,
    private val getProfileUseCase: GetProfileUseCase<UserProfileDataType>,
    private val updateProfileUseCase: UpdateProfileUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>(authResponseListenerUseCase) {

    override val response: LiveData<Event<AuthResponse<UserProfileDataType>>> =
        map(getRawResponseData()) {
            it.applyOnSuccess { data ->
                launchAsync { updateProfileUseCase(data) }
            }
        }

    val profile: LiveData<UserProfileDataType> by lazy { getProfileUseCase() }

    private val editProfileRequest = EditProfileRequest()

    fun sendEditRequest(editActions: EditProfileRequest.() -> Unit): Unit =
        with(editProfileRequest) {
            resetAll()
            editActions()
            when {
                loginParam.isNotValidBy(String::isNullOrEmpty) -> setError(LoginRequiredAuthError())
                loginParam.isNotValidBy(loginValidator) -> setError(MalformedLoginAuthError())
                emailParam.isNotValidBy(String::isNullOrEmpty) -> setError(EmailRequiredAuthError())
                emailParam.isNotValidBy(emailValidator) -> setError(MalformedEmailAuthError())
                else -> launchAsyncRequest {
                    sendEditProfileRequestUseCase(editProfileRequest)
                }
            }
        }

    private fun RequestParam<String>.isNotValidBy(validate: String.() -> Boolean): Boolean {
        return isChanged && value.orEmpty().validate()
    }

    private fun RequestParam<String>.isNotValidBy(validator: FieldValidator): Boolean {
        return isChanged && !validator.validate(value.orEmpty())
    }
}