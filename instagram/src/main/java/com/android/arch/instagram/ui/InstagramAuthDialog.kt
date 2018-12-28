package com.android.arch.instagram.ui

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import com.android.arch.instagram.R
import com.android.arch.instagram.data.InstagramUserAccount
import com.android.arch.instagram.repository.InstagramAuthService.Companion.getAccessTokenUrl
import com.android.arch.instagram.repository.AuthException.*
import com.android.arch.instagram.repository.InstagramAuthExecutor
import kotlinx.android.synthetic.main.dialog_auth_instagram.*

/**
 * Created by alexk on 12/26/18.
 * Project android-auth-pack
 */
class InstagramAuthDialog(
    context: Context,
    private val listener: AuthTokenListener,
    private val clientId: String,
    private val redirectUrl: String
) : Dialog(context) {

    interface AuthTokenListener {
        fun onAuthTokenResponse(account: InstagramUserAccount? = null, e: Exception? = null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_auth_instagram)
        setupView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupView() {
        setTitle("Instagram")
        setCancelable(true)
        setOnCancelListener {
            authExecutor.onCanceled()
            postResult(exception = OnCanceledAuthException())
        }

        webView.apply {
            clearWebView()
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            webViewClient = AuthWebViewClient()
            settings.javaScriptEnabled = true
            with(CookieManager.getInstance()) {
                if (Build.VERSION.SDK_INT >= 21) {
                    setAcceptThirdPartyCookies(webView, true)
                } else {
                    setAcceptCookie(true)
                }
            }
            loadUrl(getAccessTokenUrl(clientId, redirectUrl))
        }
    }

    private fun postResult(account: InstagramUserAccount? = null, exception: Exception? = null) {
        listener.onAuthTokenResponse(account, exception)
        dismiss()
    }

    private fun clearWebView(): Unit = with(webView) {
        stopLoading()
        clearCache(true)
        clearHistory()
        clearFormData()
    }

    private inner class AuthWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (url?.startsWith(redirectUrl) == true) {
                Log.d("OverrideUrlLoading", "Url: $url")
                getKey(url, TOKEN_KEY)
                    ?.let {
                        clearWebView()
                        progressBar.visibility = View.VISIBLE
                        authExecutor.getUserInfo(it) { response, e ->
                            progressBar.visibility = View.GONE
                            Log.d("getUserInfo", "response: $response", e)
                            postResult(response, e)
                        }
                    } ?: postError(getKey(url, ERROR_DESCRIPTION_KEY))
                return true
            }
            return false
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.d("onPageStarted", "Url: $url")
            progressBar.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            val title = webView.title
            if (!title.isNullOrEmpty()) setTitle(title)
            progressBar.visibility = View.GONE
            Log.d("onPageFinished", "url = $url")
        }

        @TargetApi(Build.VERSION_CODES.M)
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            Log.e("onReceivedError", "url: ${request?.url}. ${error?.description}")
            postError(error?.description?.toString())
        }

        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            Log.e("onReceivedError", "url: $failingUrl. $description")
            postError(description)
        }

        private fun postError(description: String?) {
            clearWebView()
            postResult(exception = FailedAuthException(description))
        }
    }

    private fun getKey(url: String, key: String): String? =
        if (!url.contains(key))
            null
        else
            url.split(key.toRegex()).getOrNull(1)

    private val authExecutor: InstagramAuthExecutor by lazy { InstagramAuthExecutor() }

    companion object {
        private const val TOKEN_KEY = "access_token="
        private const val ERROR_DESCRIPTION_KEY = "error_description="
    }

}

