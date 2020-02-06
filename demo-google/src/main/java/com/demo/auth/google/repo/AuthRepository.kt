package com.demo.auth.google.repo

import androidx.activity.ComponentActivity
import com.android.arch.auth.core.data.repository.SingleAuthRepository
import com.android.arch.auth.core.data.repository.SocialNetworkAuthRepository
import com.android.arch.auth.google.GoogleSignInService
import com.demo.auth.google.db.UserProfile
import timber.log.Timber

class AuthRepository(private val signInService: GoogleSignInService) :
    SingleAuthRepository<UserProfile>(::UserProfile),
    SocialNetworkAuthRepository<UserProfile> {

    init {
        Timber.d("init instance $this")
    }

    override fun onCreate(activity: ComponentActivity) {
        Timber.d("onCreate: activity=$activity, this=$this")
        super.onCreate(activity, signInService)
    }
}
