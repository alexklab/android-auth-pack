package com.android.arch.instagram

import android.app.Activity
import android.content.Intent
import com.android.arch.auth.core.data.entity.AuthUserProfile
import com.android.arch.auth.core.data.entity.SignInResponse
import com.android.arch.auth.core.data.entity.SocialNetworkType
import com.android.arch.auth.core.data.network.NetworkSignInService
import com.android.arch.instagram.data.InstagramUserAccount
import com.android.arch.instagram.ui.InstagramAuthDialog

/**
 * Created by alexk on 12/26/18.
 * Project android-auth-pack
 */
class InstagramSignInService(
    private val clientId: String,
    private val redirectUrl: String
) : NetworkSignInService(), InstagramAuthDialog.AuthTokenListener {

    override val socialNetworkType = SocialNetworkType.INSTAGRAM

    override fun signOut() {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}

    override fun onAuthTokenResponse(account: InstagramUserAccount?, e: Exception?) {
        postResult(SignInResponse(
            exception = e,
            errorType = getErrorType(e),
            profile = account?.let {
                AuthUserProfile(
                    id = it.id,
                    name = it.fullName,
                    picture = it.profilePicture
                )
            }
        ))
    }

    override fun signIn(activity: Activity) {
        InstagramAuthDialog(
            activity,
            listener = this,
            clientId = clientId,
            redirectUrl = redirectUrl
        ).apply { show() }
    }
}