package com.demo.auth.firebase.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.android.arch.auth.core.data.entity.AuthError
import com.android.arch.auth.core.data.entity.AuthError.*
import com.android.arch.auth.core.data.entity.AuthRequestStatus.*
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.EventObserver
import com.android.arch.auth.core.model.RecoveryPasswordViewModel
import com.demo.auth.firebase.R
import com.demo.auth.firebase.common.*
import com.demo.auth.firebase.data.entity.UserProfile
import kotlinx.android.synthetic.main.fragment_recovery_password.*
import org.koin.android.ext.android.inject

class RecoveryPasswordFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recovery_password, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.apply {
            response.observe(viewLifecycleOwner, EventObserver(::handleSendNewPasswordResponse))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emailText.onFocusChangeListener = PasswordLayoutListener(emailLayout)
        sendButton.setOnClickListener { sendRecoveryPasswordRequest() }
    }

    override fun onResume() {
        super.onResume()
        activity?.title = "Firebase: Recovery password"
    }

    private fun sendRecoveryPasswordRequest() {
        sendButton.isClickable = false
        clearAllErrors(emailLayout)
        viewModel.sendRecoveryPasswordRequest(emailText.textValue())
    }

    private fun handleSendNewPasswordResponse(response: AuthResponse<UserProfile>): Unit = with(response) {
        Log.d("handleSendNewPassword", "$response")

        fun dismissProgressAndFinishScreen() = applyContext {
            recoverProgressBar.visibility = View.GONE
            AlertDialog.Builder(this)
                .setTitle(R.string.recovery_password)
                .setMessage(R.string.message_recover_password)
                .setPositiveButton(android.R.string.ok) { _, _ -> activity?.onBackPressed() }
                .setOnCancelListener { activity?.onBackPressed() }
                .setCancelable(true)
                .create()
                .apply { setCanceledOnTouchOutside(true) }
                .show()
        }

        fun dismissProgressAndHandleError() {
            recoverProgressBar.visibility = View.GONE
            sendButton.isClickable = true
            handleResponseError(errorType = error)
            Log.w("Fail AuthResponse", "$error", error?.exception)
        }

        when (status) {
            SUCCESS -> dismissProgressAndFinishScreen()
            FAILED -> dismissProgressAndHandleError()
            ON_PROGRESS -> recoverProgressBar.visibility = View.VISIBLE
        }
    }

    private fun handleResponseError(errorType: AuthError?): Unit = when (errorType) {
        EmailRequiredAuthError -> emailLayout.error = getString(R.string.error_field_required)
        MalformedEmailAuthError -> emailLayout.error = getString(R.string.error_invalid_email)
        AccountNotFoundAuthError -> emailLayout.error = getString(R.string.error_email_not_found)
        else -> showFailRequestAlert(errorType, onRetry = ::sendRecoveryPasswordRequest)
    }

    private val viewModel: RecoveryPasswordViewModel<UserProfile> by inject()
}