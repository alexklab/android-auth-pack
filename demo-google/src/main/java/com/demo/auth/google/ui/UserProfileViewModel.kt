package com.demo.auth.google.ui

import androidx.lifecycle.LiveData
import com.android.arch.auth.core.domain.auth.NetworksSignOutUseCase
import com.android.arch.auth.core.domain.profile.DeleteProfileUseCase
import com.android.arch.auth.core.domain.profile.GetProfileUseCase
import com.android.arch.auth.core.model.BaseViewModel
import com.demo.auth.google.db.UserProfile
import timber.log.Timber
import javax.inject.Inject

class UserProfileViewModel @Inject constructor(
    getProfileUseCase: GetProfileUseCase<UserProfile>,
    private val networksSignOutUseCase: NetworksSignOutUseCase<UserProfile>,
    private val deleteProfileUseCase: DeleteProfileUseCase<UserProfile>
) : BaseViewModel() {

    init {
        Timber.d("init instance=$this")
    }

    val profile: LiveData<UserProfile> by lazy { getProfileUseCase() }

    fun logout() = launchAsync {
        networksSignOutUseCase()
        deleteProfileUseCase()
    }
}