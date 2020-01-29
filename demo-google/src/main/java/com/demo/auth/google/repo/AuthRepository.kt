package com.demo.auth.google.repo

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.common.extensions.postEvent
import com.android.arch.auth.core.data.entity.AuthRequestStatus.FAILED
import com.android.arch.auth.core.data.entity.AuthRequestStatus.SUCCESS
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.Event
import com.android.arch.auth.core.data.entity.SocialNetworkType
import com.android.arch.auth.core.data.network.OnActivityCreatedListener
import com.android.arch.auth.core.data.repository.SocialNetworkAuthRepository
import com.android.arch.auth.google.GoogleSignInService
import com.demo.auth.google.db.UserProfile

class AuthRepository(private val signInService: GoogleSignInService) :
    OnActivityCreatedListener(),
    SocialNetworkAuthRepository<UserProfile> {

    override fun onCreate(activity: ComponentActivity) {
        super.onCreate(activity)
        signInService.onCreate(activity)
    }

    override fun signInWithSocialNetwork(
        socialNetwork: SocialNetworkType,
        response: MutableLiveData<Event<AuthResponse<UserProfile>>>
    ) {
        signInService.signIn { data ->
            response.postEvent(
                AuthResponse(
                    status = if (data.profile != null) SUCCESS else FAILED,
                    error = data.error,
                    data = data.profile?.let {
                        UserProfile(
                            it
                        )
                    }
                )
            )
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        signInService.onActivityResult(requestCode, resultCode, data)
    }

    override fun signOut() = signInService.signOut()
}