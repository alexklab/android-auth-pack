package com.android.arch.auth.core.data.entity

import java.io.Serializable

data class SignInResponse(
    val profile: AuthUserProfile? = null,
    val exception: Exception? = null,
    val errorType: AuthResponseErrorType? = null,
    val token: String? = null,
    val tokenSecret: String? = null
) : Serializable