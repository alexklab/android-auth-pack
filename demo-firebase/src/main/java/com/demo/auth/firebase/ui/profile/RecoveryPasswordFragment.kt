package com.demo.auth.firebase.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.android.arch.auth.core.data.entity.AuthError
import com.android.arch.auth.core.data.entity.AuthError.*
import com.android.arch.auth.core.data.entity.AuthRequestStatus.*
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.EventObserver
import com.demo.auth.firebase.R
import com.demo.auth.firebase.common.*
import com.demo.auth.firebase.db.entity.UserProfile
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_recovery_password.*
import timber.log.Timber
import javax.inject.Inject

class RecoveryPasswordFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: RecoveryPasswordViewModel by activityViewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recovery_password, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.response.observe(
            viewLifecycleOwner,
            EventObserver(::handleSendNewPasswordResponse)
        )
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

    private fun handleSendNewPasswordResponse(response: AuthResponse<UserProfile>): Unit =
        with(response) {
            Timber.d("handleSendNewPassword: $response")

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
                Timber.w(error?.exception, "Fail AuthResponse: $error")
            }

            when (status) {
                SUCCESS -> dismissProgressAndFinishScreen()
                FAILED -> dismissProgressAndHandleError()
                ON_PROGRESS -> recoverProgressBar.visibility = View.VISIBLE
            }
        }

    private fun handleResponseError(errorType: AuthError?): Unit = when (errorType) {
        is EmailRequiredAuthError -> emailLayout.error = getString(R.string.error_field_required)
        is MalformedEmailAuthError -> emailLayout.error = getString(R.string.error_invalid_email)
        is AccountNotFoundAuthError -> emailLayout.error = getString(R.string.error_email_not_found)
        else -> showFailRequestAlert(errorType, onRetry = ::sendRecoveryPasswordRequest)
    }
}