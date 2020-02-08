package com.demo.auth.firebase.ui.profile

import com.android.arch.auth.core.common.PasswordFieldValidator
import com.android.arch.auth.core.domain.auth.AuthResponseListenerUseCase
import com.android.arch.auth.core.domain.auth.ChangePasswordUseCase
import com.android.arch.auth.core.domain.profile.GetProfileUidUseCase
import com.android.arch.auth.core.model.ChangePasswordViewModel
import com.demo.auth.firebase.db.entity.UserProfile
import timber.log.Timber
import javax.inject.Inject

class ChangePasswordViewModel @Inject constructor(
    passwordValidator: PasswordFieldValidator,
    authResponseListenerUseCase: AuthResponseListenerUseCase<UserProfile>,
    changePasswordUseCase: ChangePasswordUseCase<UserProfile>,
    getProfileUidUseCase: GetProfileUidUseCase<UserProfile>
) : ChangePasswordViewModel<UserProfile>(
    passwordValidator,
    authResponseListenerUseCase,
    changePasswordUseCase,
    getProfileUidUseCase
) {
    init {
        Timber.d("init instance=$this")
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d(" onCleared instance=$this")
    }
}