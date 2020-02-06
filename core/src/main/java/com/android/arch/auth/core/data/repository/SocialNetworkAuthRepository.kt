package com.android.arch.auth.core.data.repository

import com.android.arch.auth.core.data.entity.SocialNetworkType

interface SocialNetworkAuthRepository<UserProfileDataType> :
    BaseAuthRepository<UserProfileDataType> {

    fun signInWithSocialNetwork(socialNetwork: SocialNetworkType)

    fun signOut()
}