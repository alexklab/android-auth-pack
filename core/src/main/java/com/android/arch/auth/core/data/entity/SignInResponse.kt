package com.android.arch.auth.core.data.entity

import java.io.Serializable

data class SignInResponse(
    val profile: AuthUserProfile? = null,
    val error: AuthResponseError? = null,
    val token: String? = null,
    val tokenSecret: String? = null
) : Serializable