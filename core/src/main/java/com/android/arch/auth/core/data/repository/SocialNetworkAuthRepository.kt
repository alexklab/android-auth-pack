package com.android.arch.auth.core.data.repository

import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.Event
import com.android.arch.auth.core.data.entity.SocialNetworkType

interface SocialNetworkAuthRepository<UserProfileDataType> {

    fun signInWithSocialNetwork(
        socialNetwork: SocialNetworkType,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    )

    fun signOut()
}