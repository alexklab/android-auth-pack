package com.android.arch.auth.core.entity

/**
 * Created by alexk on 11/21/18.
 * Project android-auth-pack
 */
data class AuthResponse<UserProfileDataType>(
        val status: AuthRequestStatus,
        val errorType: AuthResponseErrorType? = null,
        val errorMessage: String? = null,
        val data: UserProfileDataType? = null) {

    fun isSuccess() = status == AuthRequestStatus.SUCCESS
}