package com.android.arch.auth.facebook

data class FacebookSignInResponse(
    val profile: FacebookProfile? = null,
    val exception: Exception? = null
)