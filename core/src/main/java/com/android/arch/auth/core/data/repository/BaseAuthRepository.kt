package com.android.arch.auth.core.data.repository

interface BaseAuthRepository<UserProfileDataType> {
    fun addListener(listener: AuthRepositoryListener<UserProfileDataType>)
    fun removeListener(listener: AuthRepositoryListener<UserProfileDataType>)
}