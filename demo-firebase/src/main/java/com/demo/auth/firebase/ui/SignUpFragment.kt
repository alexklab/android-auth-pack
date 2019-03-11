package com.demo.auth.firebase.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.arch.auth.core.data.entity.AuthError
import com.android.arch.auth.core.data.entity.AuthError.*
import com.android.arch.auth.core.data.entity.AuthRequestStatus.*
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.EventObserver
import com.android.arch.auth.core.model.SignUpViewModel
import com.demo.auth.firebase.R
import com.demo.auth.firebase.common.*
import com.demo.auth.firebase.common.PasswordFieldValidator.Companion.MIN_PASSWORD_SIZE
import com.demo.auth.firebase.data.entity.UserProfile
import kotlinx.android.synthetic.main.fragment_sign_up.*
import org.koin.android.ext.android.inject

class SignUpFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.response.observe(viewLifecycleOwner, EventObserver(::handleSignUpResponse))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateTermOfUseLayout()
        loginText.onFocusChangeListener = TextLayoutListener(loginLayout)
        loginText.onFocusChangeListener = TextLayoutListener(loginLayout)
        passwordText.onFocusChangeListener = PasswordLayoutListener(passwordLayout)
        confirmPasswordText.onFocusChangeListener = PasswordLayoutListener(confirmPasswordLayout)
        signUpButton.setOnClickListener { sendSignUpRequest() }
        termsOfUseButton.setOnClickListener { /* todo show term of use fragment */ }
        termOfUseCheckBox.setOnCheckedChangeListener { _, isChecked -> if (isChecked) updateTermOfUseLayout() }
    }

    override fun onResume() {
        super.onResume()
        activity?.title = "Firebase: Sign Up"
    }

    override fun onStop() {
        super.onStop()
        progressBar.visibility = View.GONE
    }

    private fun sendSignUpRequest() {
        signUpButton.isClickable = false
        updateTermOfUseLayout()
        clearAllErrors(loginLayout, emailLayout, passwordLayout, confirmPasswordLayout)
        viewModel.signUp(
            login = loginText.textValue(),
            email = emailText.textValue(),
            password = passwordText.textValue(),
            confirmPassword = confirmPasswordText.textValue(),
            isEnabledTermsOfUse = termOfUseCheckBox.isChecked
        )
    }

    private fun handleSignUpResponse(response: AuthResponse<UserProfile>): Unit = with(response) {
        Log.d("handleSignUpResponse", "$response")

        fun dismissProgressAndHandleError() {
            Log.w("Fail AuthResponse", "$error", error?.exception)
            progressBar.visibility = View.GONE
            signUpButton.isClickable = true
            handleErrors(error)
        }

        when (status) {
            FAILED -> dismissProgressAndHandleError()
            SUCCESS -> { /* show user profile fragment */
            }
            ON_PROGRESS -> progressBar.visibility = View.VISIBLE
        }
    }

    private fun handleErrors(errorType: AuthError?): Unit = when (errorType) {
        LoginRequiredAuthError -> loginLayout.error = getString(R.string.error_field_required)
        MalformedLoginAuthError -> loginLayout.error = getString(R.string.login_validation_error)
        LoginAlreadyExistAuthError -> loginLayout.error = getString(R.string.login_conflict_error)
        EmailRequiredAuthError -> emailLayout.error = getString(R.string.error_field_required)
        MalformedEmailAuthError -> emailLayout.error = getString(R.string.error_invalid_email)
        EmailAlreadyExistAuthError -> emailLayout.error = getString(R.string.error_email_in_use)
        PasswordRequiredAuthError -> passwordLayout.error = getString(R.string.error_field_required)
        WeakPasswordAuthError -> {
            val symbolsValue = "$MIN_PASSWORD_SIZE ${resources.getQuantityString(R.plurals.symbols, MIN_PASSWORD_SIZE)}"
            passwordLayout.error = getString(R.string.weak_password_error, symbolsValue)
        }
        WrongPasswordAuthError -> passwordLayout.error = getString(R.string.error_incorrect_password)
        ConfirmPasswordRequiredAuthError -> confirmPasswordLayout.error = getString(R.string.error_field_required)
        NotMatchedConfirmPasswordAuthError -> confirmPasswordLayout.error = getString(R.string.error_incorrect_password)
        EnableTermsOfUseAuthError -> updateTermOfUseLayout("")
        else -> showFailRequestAlert(errorType, onRetry = ::sendSignUpRequest)
    }

    private fun updateTermOfUseLayout(error: String? = null) {
        termOfUseCheckBox.error = error
        termsOfUseButton?.setTextColorRes(
            error
                ?.let { android.R.color.holo_red_dark }
                ?: android.R.color.holo_green_dark
        )
    }


    private val viewModel: SignUpViewModel<UserProfile> by inject()
}