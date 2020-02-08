package com.demo.auth.google.ui

import androidx.lifecycle.ViewModel
import com.android.arch.auth.core.model.NetworkSignInViewModel
import com.android.arch.auth.core.model.SignInWithSocialNetworksViewModel
import com.demo.auth.google.db.UserProfile
import timber.log.Timber
import javax.inject.Inject

class SignInViewModel @Inject constructor(
    viewModelDelegate: SignInWithSocialNetworksViewModel<UserProfile>
) : ViewModel(), NetworkSignInViewModel<UserProfile> by viewModelDelegate {

    init {
        Timber.d("init instance=$this")
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d(" onCleared instance=$this")
    }
}