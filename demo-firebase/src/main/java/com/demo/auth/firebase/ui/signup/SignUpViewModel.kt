package com.demo.auth.firebase.ui.signup

import com.android.arch.auth.core.common.EmailFieldValidator
import com.android.arch.auth.core.common.LoginFieldValidator
import com.android.arch.auth.core.common.PasswordFieldValidator
import com.android.arch.auth.core.domain.auth.AuthResponseListenerUseCase
import com.android.arch.auth.core.domain.auth.SignUpUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase
import com.android.arch.auth.core.model.SignUpViewModel
import com.demo.auth.firebase.db.entity.UserProfile
import timber.log.Timber
import javax.inject.Inject

class SignUpViewModel @Inject constructor(
    emailValidator: EmailFieldValidator,
    loginValidator: LoginFieldValidator,
    passwordValidator: PasswordFieldValidator,
    authResponseListenerUseCase: AuthResponseListenerUseCase<UserProfile>,
    signUpUseCase: SignUpUseCase<UserProfile>,
    updateProfileUseCase: UpdateProfileUseCase<UserProfile>
) : SignUpViewModel<UserProfile>(
    emailValidator,
    loginValidator,
    passwordValidator,
    authResponseListenerUseCase,
    signUpUseCase,
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