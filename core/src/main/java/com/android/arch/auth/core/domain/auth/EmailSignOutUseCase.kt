package com.android.arch.auth.core.domain.auth

import com.android.arch.auth.core.data.repository.EmailAuthRepository

class EmailSignOutUseCase<UserProfileDataType>(private val repository: EmailAuthRepository<UserProfileDataType>) {

    operator fun invoke(): Unit = repository.signOut()

}