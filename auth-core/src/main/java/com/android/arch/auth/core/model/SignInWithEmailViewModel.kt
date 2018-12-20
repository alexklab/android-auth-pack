package com.android.arch.auth.core.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import com.android.arch.auth.core.common.extensions.applyOnSuccess
import com.android.arch.auth.core.domain.auth.SignInWithEmailUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.AuthResponseErrorType.EMPTY_EMAIL
import com.android.arch.auth.core.data.entity.AuthResponseErrorType.EMPTY_PASSWORD
import com.android.arch.auth.core.data.entity.Event

/**
 * Created by alexk on 11/26/18.
 * Project android-auth-pack
 */
class SignInWithEmailViewModel<UserProfileDataType>(
        private val signInWithEmailUseCase: SignInWithEmailUseCase<UserProfileDataType>,
        private val updateProfileUseCase: UpdateProfileUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>() {

    override val response: LiveData<Event<AuthResponse<UserProfileDataType>>> = map(getRawResponseData()) {
        it.applyOnSuccess(updateProfileUseCase::invoke)
    }

    fun signInWithEmail(email: String, password: String): Unit = when {
        email.isEmpty() -> setError(EMPTY_EMAIL)
        password.isEmpty() -> setError(EMPTY_PASSWORD)
        else -> launchAuthTask { signInWithEmailUseCase(email, password, it) }
    }
}