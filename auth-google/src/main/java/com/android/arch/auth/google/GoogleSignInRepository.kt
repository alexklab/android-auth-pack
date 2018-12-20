package com.android.arch.auth.google

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.common.extensions.postError
import com.android.arch.auth.core.common.extensions.postEvent
import com.android.arch.auth.core.data.entity.*
import com.android.arch.auth.core.data.entity.AuthRequestStatus.*
import com.android.arch.auth.core.data.entity.AuthResponseErrorType.*
import com.android.arch.auth.core.data.repository.SocialNetworkAuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

/**
 * Created by alexk on 12/20/18.
 * Project android-auth-pack
 */
class GoogleSignInRepository<UserProfileDataType>(
    private val factory: Factory<UserProfileDataType>,
    private val service: GoogleSignInService
) : SocialNetworkAuthRepository<UserProfileDataType> {

    interface Factory<UserProfileDataType> {
        fun create(user: GoogleSignInAccount): UserProfileDataType
    }

    /**
     * Should be called on Activity.onCreate
     */
    fun onCreate(activity: ComponentActivity) {
        service.onCreate(activity)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        service.onActivityResult(requestCode, resultCode, data)
    }

    override fun signInWithSocialNetwork(
        socialNetwork: SocialNetworkType,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ): Unit = with(response) {
        service.signIn { account, _, e ->
            account
                ?.let { postEvent(AuthResponse(status = SUCCESS, data = factory.create(it))) }
                ?: postError(AUTH_CANCELED, e?.message)
        }
    }

    override fun signOut() {
        service.signOut()
    }
}

