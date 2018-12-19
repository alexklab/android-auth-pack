package com.demo.auth.core.domain.auth

import androidx.lifecycle.MutableLiveData
import com.demo.auth.core.entity.AuthResponse
import com.demo.auth.core.entity.Event
import com.demo.auth.core.repos.AuthRepository

/**
 * Created by alexk on 10/29/18.
 * Project android-auth-pack
 */
class SignInWithEmailUseCase<UserProfileDataType>(private val repository: AuthRepository<UserProfileDataType>) {

    operator fun invoke(email: String, password: String, response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>): Unit =
            repository.signInWithEmail(email, password, response)
}