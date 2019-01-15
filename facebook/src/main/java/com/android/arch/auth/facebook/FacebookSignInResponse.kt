package com.android.arch.auth.facebook

import com.android.arch.auth.core.data.entity.AuthResponseErrorType

data class FacebookSignInResponse(
    val profile: FacebookProfile? = null,
    val exception: Exception? = null,
    val errorType: AuthResponseErrorType? = null
)