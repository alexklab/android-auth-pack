package com.android.arch.auth.core.data.entity

data class AuthUserProfile(
    val id: String,
    val name: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val picture: String? = null
)