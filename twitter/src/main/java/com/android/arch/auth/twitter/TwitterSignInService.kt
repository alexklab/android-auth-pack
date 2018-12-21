package com.android.arch.auth.twitter

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import com.android.arch.auth.core.data.entity.SocialNetworkType.TWITTER
import com.android.arch.auth.core.data.network.NetworkSignInService
import com.android.arch.auth.core.data.network.ParamsBundle
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterLoginButton

/**
 * Created by alexk on 12/20/18.
 * Project android-auth-pack
 */
class TwitterSignInService(
    private val consumerApiKey: String,
    private val consumerApiSecretKey: String
) : NetworkSignInService<TwitterSession>() {

    override val socialNetworkType = TWITTER

    private var twitterLoginButton: TwitterLoginButton? = null

    override fun onCreate(activity: ComponentActivity) {
        super.onCreate(activity)
        val authConfig = TwitterAuthConfig(consumerApiKey, consumerApiSecretKey)

        val twitterConfig = TwitterConfig.Builder(activity)
            .twitterAuthConfig(authConfig)
            .build()

        Twitter.initialize(twitterConfig)

        twitterLoginButton = TwitterLoginButton(activity).apply {
            callback = object : Callback<TwitterSession>() {
                override fun success(result: Result<TwitterSession>) = postResult(result.data)
                override fun failure(e: TwitterException) = postResult(exception = e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        twitterLoginButton = null
    }

    override fun signIn(activity: Activity) {
        twitterLoginButton?.let { button ->
            Handler(Looper.getMainLooper()).apply {
                post { button.callOnClick() }
            }
        } ?: Log.w("signIn", "Unassigned state: twitterLoginButton = null")
    }

    override fun signOut() {
        TwitterCore.getInstance().sessionManager.clearActiveSession()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        twitterLoginButton?.onActivityResult(requestCode, resultCode, data)
            ?: Log.e("onActivityResult", "Unassigned state: twitterLoginButton = null")
    }

    override fun getParamsBundle(data: TwitterSession) = ParamsBundle(data.authToken.token, data.authToken.secret)
}