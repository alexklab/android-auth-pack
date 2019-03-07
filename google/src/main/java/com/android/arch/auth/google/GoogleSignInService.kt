package com.android.arch.auth.google

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import com.android.arch.auth.core.data.entity.AuthResponseError
import com.android.arch.auth.core.data.entity.AuthUserProfile
import com.android.arch.auth.core.data.entity.SignInResponse
import com.android.arch.auth.core.data.entity.SocialNetworkType.GOOGLE
import com.android.arch.auth.core.data.network.NetworkSignInService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status

/**
 * Created by alexk on 12/19/18.
 * Project android-auth-pack
 */
class GoogleSignInService(
    private val webClientId: String,
    private val signInRequestCode: Int = RC_SIGN_IN
) : NetworkSignInService() {

    override val socialNetworkType = GOOGLE

    private var googleSignInClient: GoogleSignInClient? = null

    private companion object {

        const val RC_SIGN_IN = 2040
    }

    override fun getErrorType(exception: Exception?): AuthResponseError? = exception?.let {
        when (it) {
            is GoogleSignInCanceledException -> AuthResponseError.Canceled
            else -> AuthResponseError.ServiceError("Google SignIn Error: ${exception.message}", exception = exception)
        }
    }

    /**
     * Should be called in Activity.onCreate method
     */
    override fun onCreate(activity: ComponentActivity) {
        super.onCreate(activity)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    /**
     * Should be called in Activity.onDestroy method
     */
    override fun onDestroy() {
        super.onDestroy()
        googleSignInClient = null
    }

    override fun signIn(activity: Activity) {
        googleSignInClient
            ?.let { activity.startActivityForResult(it.signInIntent, signInRequestCode) }
            ?: Log.w("signIn", "not attached. googleSignInClient = null")
    }

    override fun signOut() {
        googleSignInClient?.signOut()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != signInRequestCode) {
            Log.w("onActivityResult", "wrong request code: $requestCode")
            return
        }

        if (resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                handleSignInResult(account)
            } catch (e: ApiException) {
                Log.w("onActivityResult", "Google sign in failed", e)
                handleSignInResult(exception = e)
            }
        } else {
            handleSignInResult(exception = GoogleSignInCanceledException())
        }
    }

    private fun handleSignInResult(account:GoogleSignInAccount? = null, exception: Exception? = null){
        postResult(SignInResponse(
            token = account?.idToken,
            error = getErrorType(exception),
            profile = account?.let{
                AuthUserProfile(
                    id = it.id.orEmpty(),
                    email = it.email,
                    name = it.displayName.orEmpty(),
                    firstName = it.givenName,
                    lastName = it.familyName,
                    picture = it.photoUrl?.toString()
                )
            }
        ))
    }

    class GoogleSignInCanceledException : ApiException(Status.RESULT_CANCELED)
}