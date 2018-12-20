package com.demo.auth.firebase.data.network

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import com.demo.auth.core.entity.SocialNetworkType.TWITTER
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterLoginButton

/**
 * Created by alexk on 12/20/18.
 * Project android-auth-pack
 */
class TwitterSignInService : NetworkSignInService<TwitterSession>() {

    override val socialNetworkType = TWITTER
    private var twitterLoginButton: TwitterLoginButton? = null

    private lateinit var consumerApiKey: String
    private lateinit var consumerApiSecretKey: String

    private companion object {
        const val META_CONSUMER_API_KEY = "com.android.arch.auth.TwitterConsumerApiKey"
        const val META_CONSUMER_API_SECRET_KEY = "com.android.arch.auth.TwitterConsumerApiSecretKey"
    }

    override fun onCreate(activity: ComponentActivity) {
        super.onCreate(activity)
        applyTwitterApiKey(activity)
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
            ?: Log.w("onActivityResult", "Unassigned state: twitterLoginButton = null")
    }

    private fun applyTwitterApiKey(activity: ComponentActivity) = with(activity) {
        val data = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)?.metaData
        consumerApiKey = data?.getString(META_CONSUMER_API_KEY)
                ?: throw IllegalArgumentException("Meta data not found: $META_CONSUMER_API_KEY")
        consumerApiSecretKey = data.getString(META_CONSUMER_API_SECRET_KEY) ?:
                throw IllegalArgumentException("Meta data not found: $META_CONSUMER_API_KEY")
    }
}