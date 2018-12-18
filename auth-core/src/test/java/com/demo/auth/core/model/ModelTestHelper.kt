package com.demo.auth.core.model

import com.demo.auth.core.entity.AuthRequestStatus
import com.demo.auth.core.entity.AuthRequestStatus.FAILED
import com.demo.auth.core.entity.AuthRequestStatus.SUCCESS
import com.demo.auth.core.entity.AuthResponse
import com.demo.auth.core.entity.AuthResponseErrorType


data class UserProfile(
        val uid: String? = null,
        val email: String? = null,
        val login: String? = null
)

fun <DataType> AuthResponseErrorType?.toAuthResponse(profile: DataType? = null): AuthResponse<DataType> {
    val data: DataType? = this?.let { null } ?: profile
    val status: AuthRequestStatus = this?.let { FAILED } ?: SUCCESS
    return AuthResponse(status, this, data = data)
}