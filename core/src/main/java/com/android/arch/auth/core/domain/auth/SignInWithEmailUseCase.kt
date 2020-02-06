package com.android.arch.auth.core.domain.auth

import com.android.arch.auth.core.data.repository.EmailAuthRepository

/**
 * Created by alexk on 10/29/18.
 * Project android-auth-pack
 */
class SignInWithEmailUseCase<UserProfileDataType>(private val repository: EmailAuthRepository<UserProfileDataType>) {

    operator fun invoke(email: String, password: String): Unit =
        repository.signInWithEmail(email, password)
}