package com.demo.auth.firebase.data.network

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import com.demo.auth.core.entity.SocialNetworkType.GOOGLE
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

/**
 * Created by alexk on 12/19/18.
 * Project android-auth-pack
 */
class GoogleSignInService(
    private val signInRequestCode: Int = RC_SIGN_IN
) : NetworkSignInService<GoogleSignInAccount>() {

    override val socialNetworkType = GOOGLE
    private lateinit var webClientId: String
    private var googleSignInClient: GoogleSignInClient? = null

    private companion object {
        const val RC_SIGN_IN = 2040
        const val WEB_CLIENT_ID_META = "com.android.arch.auth.GoogleWebClientId"
    }

    /**
     * Should be called in Activity.onCreate method
     */
    override fun onCreate(activity: ComponentActivity) {
        super.onCreate(activity)
        applyGoogleWebClientId(activity)

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
                postResult(account)
            } catch (e: ApiException) {
                Log.w("onActivityResult", "Google sign in failed", e)
                postResult(exception = e)
            }
        } else {
            postResult()
        }
    }

    private fun applyGoogleWebClientId(activity: ComponentActivity) = with(activity) {
        webClientId = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)?.metaData
            ?.getString(WEB_CLIENT_ID_META)
                ?: throw IllegalArgumentException("Meta data not found: $WEB_CLIENT_ID_META")
    }
}