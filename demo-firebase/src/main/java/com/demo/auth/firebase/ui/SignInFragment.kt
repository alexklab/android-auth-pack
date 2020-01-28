package com.demo.auth.firebase.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.arch.auth.core.data.entity.AuthError
import com.android.arch.auth.core.data.entity.AuthError.*
import com.android.arch.auth.core.data.entity.AuthRequestStatus.*
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.EventObserver
import com.android.arch.auth.core.data.entity.SocialNetworkType
import com.android.arch.auth.core.data.entity.SocialNetworkType.*
import com.android.arch.auth.core.model.SignInWithEmailViewModel
import com.android.arch.auth.core.model.SignInWithSocialNetworksViewModel
import com.demo.auth.firebase.MainActivity
import com.demo.auth.firebase.R
import com.demo.auth.firebase.common.*
import com.demo.auth.firebase.data.entity.UserProfile
import kotlinx.android.synthetic.main.fragment_sign_in.*
import org.koin.android.ext.android.inject

/**
 * Created by alexk on 11/26/18.
 * Project sportuaappandroid
 */
class SignInFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_sign_in, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        signInWithEmailViewModel.response.observe(viewLifecycleOwner, EventObserver(::handleSignInResponse))
        signInWithSocialNetworksViewModel.response.observe(viewLifecycleOwner, EventObserver(::handleSignInResponse))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emailText.onFocusChangeListener = TextLayoutListener(emailLayout)
        passwordText.onFocusChangeListener = PasswordLayoutListener(passwordLayout)
        signInWithEmailButton.setOnClickListener { sendSignInWithEmailRequest() }
        googleSignInButton.setOnClickListener { sendSignInWithSocialNetwork(GOOGLE) }
        facebookSignInButton.setOnClickListener { sendSignInWithSocialNetwork(FACEBOOK) }
        twitterSignInButton.setOnClickListener { sendSignInWithSocialNetwork(TWITTER) }
        signUpButton.setOnClickListener { mainActivity?.addFragment(SignUpFragment()) }
        forgotPasswordButton.setOnClickListener { mainActivity?.addFragment(RecoveryPasswordFragment()) }
    }

    override fun onResume() {
        super.onResume()
        activity?.title = "Firebase: Sign In"
    }

    private fun sendSignInWithSocialNetwork(socialNetworkType: SocialNetworkType) {
        setButtonsClickable(isClickable = false)
        clearAllErrors(passwordLayout, emailLayout)
        signInWithSocialNetworksViewModel.signInWithSocialNetwork(socialNetworkType)
    }

    private fun sendSignInWithEmailRequest() {
        setButtonsClickable(isClickable = false)
        clearAllErrors(passwordLayout, emailLayout)
        signInWithEmailViewModel.signInWithEmail(emailText.textValue(), passwordText.textValue())
    }

    private fun setButtonsClickable(isClickable: Boolean) = setAllClickable(
        isClickable,
        signInWithEmailButton, signUpButton, forgotPasswordButton,
        googleSignInButton, facebookSignInButton, twitterSignInButton
    )

    private fun setAllClickable(isClickable: Boolean, vararg views: View?) {
        views.forEach { it?.isClickable = isClickable }
    }

    private fun handleSignInResponse(response: AuthResponse<UserProfile>): Unit = with(response) {
        Log.d("handleSignInResponse:", "$response")

        fun handleErrors(errorType: AuthError?) = when (errorType) {
            is EmailRequiredAuthError -> emailLayout.error = getString(R.string.error_field_required)
            is PasswordRequiredAuthError -> passwordLayout.error = getString(R.string.error_field_required)
            is CanceledAuthError -> setButtonsClickable(isClickable = true)
            is WrongPasswordAuthError -> passwordLayout.error = getString(R.string.error_incorrect_password)
            is AccountNotFoundAuthError -> emailLayout.error = getString(R.string.error_email_not_found)
            is AccountNotActivatedAuthError -> emailLayout.error = getString(R.string.error_email_not_activated)
            is TooManyRequestsAuthError -> showTooManyRequestAlert(errorType)
            else -> showFailRequestAlert(errorType, onRetry = ::sendSignInWithEmailRequest)
        }

        fun dismissProgressAndHandleError() {
            Log.w("Fail AuthResponse:", "$error", error?.exception)
            Toast.makeText(context, "${error?.errorName}", Toast.LENGTH_LONG).show()
            progressBar.visibility = View.GONE
            setButtonsClickable(isClickable = true)
            handleErrors(error)
        }

        when (status) {
            FAILED -> dismissProgressAndHandleError()
            SUCCESS -> { /* replace with userProfileFragment */
            }
            ON_PROGRESS -> progressBar.visibility = View.VISIBLE
        }
    }

    private val mainActivity: MainActivity? get() = activity as? MainActivity
    private val signInWithEmailViewModel: SignInWithEmailViewModel<UserProfile> by inject()
    private val signInWithSocialNetworksViewModel: SignInWithSocialNetworksViewModel<UserProfile> by inject()
}