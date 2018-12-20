package com.android.arch.auth.core.repos

import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.entity.AuthResponse
import com.android.arch.auth.core.entity.Event
import com.android.arch.auth.core.entity.SocialNetworkType

/**
 * Created by alexk on 11/21/18.
 * Project android-auth-pack
 */
interface AuthRepository<UserProfileDataType> {

    fun changePassword(
        uid: String,
        oldPassword: String,
        newPassword: String,
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

    fun signInWithSocialNetwork(
        socialNetwork: SocialNetworkType,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    )

    fun sendUpdateProfileRequest(
        uid: String,
        login: String,
        email: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    )

    fun sendVerifiedEmailKeyUseCase(
        verifyKey: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    )

    fun signOut()

}