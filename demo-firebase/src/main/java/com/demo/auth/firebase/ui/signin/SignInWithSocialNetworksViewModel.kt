package com.demo.auth.firebase.ui.signin

import com.android.arch.auth.core.domain.auth.AuthResponseListenerUseCase
import com.android.arch.auth.core.domain.auth.SignInWithSocialNetworkUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase
import com.android.arch.auth.core.model.SignInWithSocialNetworksViewModel
import com.demo.auth.firebase.db.entity.UserProfile
import timber.log.Timber
import javax.inject.Inject

class SignInWithSocialNetworksViewModel @Inject constructor(
    authResponseListenerUseCase: AuthResponseListenerUseCase<UserProfile>,
    signInWithSocialNetworkUseCase: SignInWithSocialNetworkUseCase<UserProfile>,
    updateProfileUseCase: UpdateProfileUseCase<UserProfile>
) : SignInWithSocialNetworksViewModel<UserProfile>(
    authResponseListenerUseCase,
    signInWithSocialNetworkUseCase,
    updateProfileUseCase
) {

    init {
        Timber.d("init instance=$this")
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d(" onCleared instance=$this")
    }
}