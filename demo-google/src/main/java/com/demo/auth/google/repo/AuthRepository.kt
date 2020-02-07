package com.demo.auth.google.repo

import com.android.arch.auth.core.data.repository.SingleAuthRepository
import com.android.arch.auth.google.GoogleSignInService
import com.demo.auth.google.db.UserProfile
import timber.log.Timber

class AuthRepository(signInService: GoogleSignInService) :
    SingleAuthRepository<UserProfile>(::UserProfile, signInService) {

    init {
        Timber.d("init instance $this")
    }

}
