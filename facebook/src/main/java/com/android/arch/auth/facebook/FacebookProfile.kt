package com.android.arch.auth.facebook

data class FacebookProfile(
    val id: String,
    val name: String,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val picture: String?
)