package com.android.arch.auth.core.data.entity

/**
 * Created by alexk on 11/21/18.
 * Project android-auth-pack
 */
data class AuthResponse<UserProfileDataType>(
    val status: AuthRequestStatus,
    val error: AuthError? = null,
    val data: UserProfileDataType? = null
) {

    fun isSuccess() = status == AuthRequestStatus.SUCCESS
}