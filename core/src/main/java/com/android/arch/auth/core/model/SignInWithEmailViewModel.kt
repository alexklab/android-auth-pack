package com.android.arch.auth.core.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import com.android.arch.auth.core.common.extensions.applyOnSuccess
import com.android.arch.auth.core.data.entity.AuthError.EmailRequiredAuthError
import com.android.arch.auth.core.data.entity.AuthError.PasswordRequiredAuthError
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.Event
import com.android.arch.auth.core.domain.auth.AuthResponseListenerUseCase
import com.android.arch.auth.core.domain.auth.SignInWithEmailUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase

/**
 * Created by alexk on 11/26/18.
 * Project android-auth-pack
 */
interface EmailSignInViewModel<UserProfileDataType> {
    val response: LiveData<Event<AuthResponse<UserProfileDataType>>>
    fun signInWithEmail(email: String, password: String)
}

open class SignInWithEmailViewModel<UserProfileDataType>(
    authResponseListenerUseCase: AuthResponseListenerUseCase<UserProfileDataType>,
    private val signInWithEmailUseCase: SignInWithEmailUseCase<UserProfileDataType>,
    private val updateProfileUseCase: UpdateProfileUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>(authResponseListenerUseCase),
    EmailSignInViewModel<UserProfileDataType> {

    override val response: LiveData<Event<AuthResponse<UserProfileDataType>>> =
        map(getRawResponseData()) {
            it.applyOnSuccess { data ->
                launchAsync { updateProfileUseCase(data) }
            }
        }

    override fun signInWithEmail(email: String, password: String): Unit = when {
        email.isEmpty() -> setError(EmailRequiredAuthError())
        password.isEmpty() -> setError(PasswordRequiredAuthError())
        else -> launchAsyncRequest { signInWithEmailUseCase(email, password) }
    }
}