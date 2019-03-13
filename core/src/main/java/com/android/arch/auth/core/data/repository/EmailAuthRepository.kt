package com.android.arch.auth.core.data.repository

import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.EditProfileRequest
import com.android.arch.auth.core.data.entity.Event

/**
 * Created by alexk on 11/21/18.
 * Project android-auth-pack
 */
interface EmailAuthRepository<UserProfileDataType> {

    fun changePassword(
        uid: String,
        oldPassword: String,
        newPassword: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    )

    fun editProfile(
        request: EditProfileRequest,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    )

    fun recoverPassword(email: String, response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>)

    fun signUp(
        login: String,
        email: String,
        password: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    )

    fun signInWithEmail(
        email: String,
        password: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    )

    fun sendVerifiedEmailKeyUseCase(
        verifyKey: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    )

    fun signOut()

}