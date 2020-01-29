package com.demo.auth.google.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.android.arch.auth.core.domain.auth.NetworksSignOutUseCase
import com.android.arch.auth.core.domain.profile.DeleteProfileUseCase
import com.android.arch.auth.core.domain.profile.GetProfileUseCase
import com.demo.auth.google.db.UserProfile
import javax.inject.Inject

class UserProfileViewModel @Inject constructor(
    getProfileUseCase: GetProfileUseCase<UserProfile>,
    private val networksSignOutUseCase: NetworksSignOutUseCase<UserProfile>,
    private val deleteProfileUseCase: DeleteProfileUseCase<UserProfile>
) : ViewModel() {

    val profile: LiveData<UserProfile> by lazy { getProfileUseCase() }

    fun logout() {
        networksSignOutUseCase()
        deleteProfileUseCase()
    }
}