package com.demo.auth.core.model

import com.demo.auth.core.domain.auth.SendVerifiedEmailKeyUseCase

class VerifyEmailViewModel<UserProfileDataType>(
        private val sendVerifiedEmailKeyUseCase: SendVerifiedEmailKeyUseCase<UserProfileDataType>
) : AuthBaseViewModel<UserProfileDataType>() {

    private var verifyEmailKey: String = ""

    fun sendVerifyEmailRequest(verifyKey: String = verifyEmailKey) {
        verifyEmailKey = verifyKey
        launchAuthTask { sendVerifiedEmailKeyUseCase(verifyKey, it) }
    }
}