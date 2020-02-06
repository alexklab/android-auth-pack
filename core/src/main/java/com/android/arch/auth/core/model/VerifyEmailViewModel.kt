package com.android.arch.auth.core.model

import com.android.arch.auth.core.domain.auth.AuthResponseListenerUseCase
import com.android.arch.auth.core.domain.auth.SendVerifiedEmailKeyUseCase

class VerifyEmailViewModel<UserProfileDataType>(
    authResponseListenerUseCase: AuthResponseListenerUseCase<UserProfileDataType>,
    private val sendVerifiedEmailKeyUseCase: SendVerifiedEmailKeyUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>(authResponseListenerUseCase) {

    private var verifyEmailKey: String = ""

    fun sendVerifyEmailRequest(verifyKey: String = verifyEmailKey) {
        verifyEmailKey = verifyKey
        launchAsyncRequest { sendVerifiedEmailKeyUseCase(verifyKey) }
    }
}