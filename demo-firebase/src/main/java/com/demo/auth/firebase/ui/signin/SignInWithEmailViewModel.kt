package com.demo.auth.firebase.ui.signin

import com.android.arch.auth.core.domain.auth.AuthResponseListenerUseCase
import com.android.arch.auth.core.domain.auth.SignInWithEmailUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase
import com.android.arch.auth.core.model.SignInWithEmailViewModel
import com.demo.auth.firebase.db.entity.UserProfile
import timber.log.Timber
import javax.inject.Inject

class SignInWithEmailViewModel @Inject constructor(
    authResponseListenerUseCase: AuthResponseListenerUseCase<UserProfile>,
    signInWithEmailUseCase: SignInWithEmailUseCase<UserProfile>,
    updateProfileUseCase: UpdateProfileUseCase<UserProfile>
) : SignInWithEmailViewModel<UserProfile>(
    authResponseListenerUseCase,
    signInWithEmailUseCase,
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