package com.android.arch.auth.core.data.repository

import com.android.arch.auth.core.data.entity.EditProfileRequest

/**
 * Created by alexk on 11/21/18.
 * Project android-auth-pack
 */
interface EmailAuthRepository<UserProfileDataType> : BaseAuthRepository<UserProfileDataType> {

    fun changePassword(
        uid: String,
        oldPassword: String,
        newPassword: String
    )

    fun editProfile(request: EditProfileRequest)

    fun recoverPassword(email: String)

    fun signUp(
        login: String,
        email: String,
        password: String
    )

    fun signInWithEmail(
        email: String,
        password: String
    )

    fun sendVerifiedEmailKeyUseCase(verifyKey: String)

    fun signOut()

}