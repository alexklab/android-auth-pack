package com.demo.auth.google.ui

import androidx.lifecycle.ViewModel
import com.android.arch.auth.core.model.SignInViewModel
import com.android.arch.auth.core.model.SignInWithSocialNetworksViewModel
import com.demo.auth.google.db.UserProfile
import javax.inject.Inject

class SignInViewModel @Inject constructor(
    viewModelDelegate: SignInWithSocialNetworksViewModel<UserProfile>
) : ViewModel(), SignInViewModel<UserProfile> by viewModelDelegate