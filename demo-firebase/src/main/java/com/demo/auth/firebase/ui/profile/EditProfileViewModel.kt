package com.demo.auth.firebase.ui.profile

import com.android.arch.auth.core.common.EmailFieldValidator
import com.android.arch.auth.core.common.LoginFieldValidator
import com.android.arch.auth.core.domain.auth.AuthResponseListenerUseCase
import com.android.arch.auth.core.domain.auth.SendEditProfileRequestUseCase
import com.android.arch.auth.core.domain.profile.GetProfileUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase
import com.android.arch.auth.core.model.EditProfileViewModel
import com.demo.auth.firebase.db.entity.UserProfile
import timber.log.Timber
import javax.inject.Inject

class EditProfileViewModel @Inject constructor(
    emailValidator: EmailFieldValidator,
    loginValidator: LoginFieldValidator,
    authResponseListenerUseCase: AuthResponseListenerUseCase<UserProfile>,
    sendEditProfileRequestUseCase: SendEditProfileRequestUseCase<UserProfile>,
    getProfileUseCase: GetProfileUseCase<UserProfile>,
    updateProfileUseCase: UpdateProfileUseCase<UserProfile>
) : EditProfileViewModel<UserProfile>(
    emailValidator,
    loginValidator,
    authResponseListenerUseCase,
    sendEditProfileRequestUseCase,
    getProfileUseCase,
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