package com.android.arch.auth.core.data.repository

import com.android.arch.auth.core.data.entity.AuthResponse

interface AuthRepositoryListener<UserProfileDataType> {

    fun onAuthResponse(response: AuthResponse<UserProfileDataType>)

}