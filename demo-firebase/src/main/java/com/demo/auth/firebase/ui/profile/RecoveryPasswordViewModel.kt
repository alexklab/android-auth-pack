package com.demo.auth.firebase.ui.profile

import com.android.arch.auth.core.common.EmailFieldValidator
import com.android.arch.auth.core.domain.auth.AuthResponseListenerUseCase
import com.android.arch.auth.core.domain.auth.RecoveryPasswordUseCase
import com.android.arch.auth.core.model.RecoveryPasswordViewModel
import com.demo.auth.firebase.db.entity.UserProfile
import timber.log.Timber
import javax.inject.Inject

class RecoveryPasswordViewModel @Inject constructor(
    emailValidator: EmailFieldValidator,
    authResponseListenerUseCase: AuthResponseListenerUseCase<UserProfile>,
    recoveryPasswordUseCase: RecoveryPasswordUseCase<UserProfile>
) : RecoveryPasswordViewModel<UserProfile>(
    emailValidator,
    authResponseListenerUseCase,
    recoveryPasswordUseCase
) {

    init {
        Timber.d("init instance=$this")
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d(" onCleared instance=$this")
    }
}