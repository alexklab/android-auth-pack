package com.android.arch.auth.data.repository

import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.entity.AuthResponse
import com.android.arch.auth.core.entity.Event
import com.android.arch.auth.core.entity.SocialNetworkType
import com.android.arch.auth.core.repos.AuthRepository
import com.android.arch.auth.data.entity.UserProfile

class FirebaseAuthRepository : AuthRepository<UserProfile> {

    override fun changePassword(
        uid: String,
        oldPassword: String,
        newPassword: String,
        response: MutableLiveData<Event<AuthResponse<UserProfile>>>
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun recoverPassword(email: String, response: MutableLiveData<Event<AuthResponse<UserProfile>>>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun signUp(
        login: String,
        email: String,
        password: String,
        response: MutableLiveData<Event<AuthResponse<UserProfile>>>
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun signInWithEmail(
        email: String,
        password: String,
        response: MutableLiveData<Event<AuthResponse<UserProfile>>>
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun signInWithSocialNetwork(
        socialNetwork: SocialNetworkType,
        response: MutableLiveData<Event<AuthResponse<UserProfile>>>
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendUpdateProfileRequest(
        uid: String,
        login: String,
        email: String,
        response: MutableLiveData<Event<AuthResponse<UserProfile>>>
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendVerifiedEmailKeyUseCase(
        verifyKey: String,
        response: MutableLiveData<Event<AuthResponse<UserProfile>>>
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}