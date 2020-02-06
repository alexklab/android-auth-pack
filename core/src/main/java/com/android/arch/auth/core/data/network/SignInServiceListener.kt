package com.android.arch.auth.core.data.network

import com.android.arch.auth.core.data.entity.SignInResponse
import com.android.arch.auth.core.data.entity.SocialNetworkType

interface SignInServiceListener {
    fun onSignInResponse(socialNetwork: SocialNetworkType, response: SignInResponse)
}