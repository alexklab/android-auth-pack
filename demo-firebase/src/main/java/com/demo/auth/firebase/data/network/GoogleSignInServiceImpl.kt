package com.demo.auth.firebase.data.network

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import java.lang.Exception

/**
 * Created by alexk on 12/19/18.
 * Project android-auth-pack
 */
class GoogleSignInServiceImpl(
    private val webClientId: String,
    private val signInRequestCode: Int = RC_SIGN_IN
) : GoogleSignInService {

    private var googleSignInClient: GoogleSignInClient? = null

    /**
     * Should be called in Activity.onCreate method
     */
    fun onCreate(activity: Activity) {
        this.activity = activity
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    /**
     * Should be called in Activity.onDestroy method
     */
    fun onDestroy() {
        activity = null
        signInCallback = null
        googleSignInClient = null
    }

    override fun signIn(callback: GoogleSignInCallBack) {
        activity?.apply {
            googleSignInClient?.let {
                signInCallback = callback
                startActivityForResult(it.signInIntent, signInRequestCode)
            } ?: Log.w("signIn:", "not attached. googleSignInClient = null")
        } ?: Log.w("signIn:", "not attached. activity = null")
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != signInRequestCode) {
            Log.w("onActivityResult:", "wrong request code: $requestCode")
            return
        }

        if (resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                postResult(account)
            } catch (e: ApiException) {
                Log.w("onActivityResult:", "Google sign in failed", e)
                postResult(exception = e)
            }
        } else {
            postResult()
        }
    }

    private fun postResult(account: GoogleSignInAccount? = null, exception: Exception? = null) {
        signInCallback
            ?.let { it(account, exception) }
            ?: Log.w("postResult:", "Wrong state. signInCallback = null")
    }

    private var activity: Activity? = null
    private var signInCallback: GoogleSignInCallBack? = null

    companion object {
        private const val RC_SIGN_IN = 2040
    }
}