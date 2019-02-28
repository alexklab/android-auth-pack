package com.demo.auth.google.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.arch.auth.core.data.entity.AuthRequestStatus.*
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.AuthResponseErrorType
import com.android.arch.auth.core.data.entity.EventObserver
import com.android.arch.auth.core.data.entity.SocialNetworkType
import com.android.arch.auth.core.model.SignInWithSocialNetworksViewModel
import com.demo.auth.google.R
import com.demo.auth.google.common.showFailRequestAlert
import com.demo.auth.google.entity.UserProfile
import kotlinx.android.synthetic.main.fragment_sign_in.*
import org.koin.android.ext.android.inject

class SignInFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.response.observe(this, EventObserver(::handleSignInResponse))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signInButton.setOnClickListener { sendSignInWithGoogleRequest() }
    }

    private fun sendSignInWithGoogleRequest() {
        signInButton.isClickable = false
        viewModel.signInWithSocialNetwork(SocialNetworkType.GOOGLE)
    }

    private fun handleSignInResponse(response: AuthResponse<UserProfile>): Unit = with(response) {
        Log.d("handleSignInResponse:", "$response")

        fun dismissProgressAndHandleError() {
            Log.w("Fail AuthResponse:", "$errorType. Cause: '$errorMessage'")
            Toast.makeText(context, "$errorType. $errorMessage", Toast.LENGTH_LONG).show()
            progressBar.visibility = View.GONE
            signInButton.isClickable = true
            if (errorType != AuthResponseErrorType.AUTH_CANCELED) {
                showFailRequestAlert(errorType, onRetry = ::sendSignInWithGoogleRequest)
            }
        }

        fun finish() {
            progressBar.visibility = View.GONE
            signInButton.isClickable = false
        }

        when (status) {
            SUCCESS -> finish()
            FAILED -> dismissProgressAndHandleError()
            ON_PROGRESS -> progressBar.visibility = View.VISIBLE
        }
    }

    private val viewModel: SignInWithSocialNetworksViewModel<UserProfile> by inject()
}