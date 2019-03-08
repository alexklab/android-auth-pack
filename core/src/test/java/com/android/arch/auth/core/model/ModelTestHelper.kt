package com.android.arch.auth.core.model

import com.android.arch.auth.core.data.entity.AuthRequestStatus
import com.android.arch.auth.core.data.entity.AuthRequestStatus.FAILED
import com.android.arch.auth.core.data.entity.AuthRequestStatus.SUCCESS
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.AuthError


data class UserProfile(
        val uid: String? = null,
        val email: String? = null,
        val login: String? = null
)

fun <DataType> AuthError?.toAuthResponse(profile: DataType? = null): AuthResponse<DataType> {
    val data: DataType? = this?.let { null } ?: profile
    val status: AuthRequestStatus = this?.let { FAILED } ?: SUCCESS
    return AuthResponse(status, this, data = data)
}