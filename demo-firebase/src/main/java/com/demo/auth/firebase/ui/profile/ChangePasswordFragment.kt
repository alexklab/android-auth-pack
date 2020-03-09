package com.demo.auth.firebase.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.demo.auth.firebase.common.PasswordFieldValidatorImpl.Companion.MIN_PASSWORD_SIZE
import com.demo.auth.firebase.db.entity.UserProfile
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_change_password.*
import timber.log.Timber
import javax.inject.Inject

class ChangePasswordFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: ChangePasswordViewModel by activityViewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.response.observe(
            viewLifecycleOwner,
            EventObserver(::handleChangePasswordResponse)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        oldPasswordText.onFocusChangeListener = PasswordLayoutListener(oldPasswordLayout)
        passwordText.onFocusChangeListener = PasswordLayoutListener(passwordLayout)
        confirmPasswordText.onFocusChangeListener = PasswordLayoutListener(confirmPasswordLayout)
        sendButton.setOnClickListener { sendChangePasswordRequest() }
    }

    override fun onResume() {
        super.onResume()
        activity?.title = "Firebase: Change password"
    }

    private fun sendChangePasswordRequest() {
        sendButton.isClickable = false
        clearAllErrors(oldPasswordLayout, passwordLayout, confirmPasswordLayout)
        viewModel.changePassword(
            oldPassword = oldPasswordText.textValue(),
            newPassword = passwordText.textValue(),
            newConfirmPassword = confirmPasswordText.textValue()
        )
    }

    private fun handleChangePasswordResponse(response: AuthResponse<UserProfile>): Unit =
        with(response) {
            Timber.d("handleChangePassword: $response")

            fun dismissProgressAndFinishScreen() = applyContext {
                recoverProgressBar.visibility = View.GONE
                AlertDialog.Builder(this)
                    .setTitle(R.string.change_password)
                    .setMessage(R.string.message_change_password)
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
                Toast.makeText(
                    requireContext(),
                    "Error: ${error?.errorName}: ${error?.exception?.message ?: error?.message}",
                    Toast.LENGTH_LONG
                ).show()
                Timber.w(error?.exception, "Fail AuthResponse: $error")
            }

            when (status) {
                SUCCESS -> dismissProgressAndFinishScreen()
                FAILED -> dismissProgressAndHandleError()
                ON_PROGRESS -> recoverProgressBar.visibility = View.VISIBLE
            }
        }

    private fun handleResponseError(errorType: AuthError?): Unit = when (errorType) {
        is OldPasswordRequiredAuthError -> oldPasswordLayout.error =
            getString(R.string.error_field_required)
        is PasswordRequiredAuthError -> passwordLayout.error =
            getString(R.string.error_field_required)
        is WeakPasswordAuthError -> passwordLayout.error = weakPasswordErrorMessage
        is WrongPasswordAuthError -> oldPasswordLayout.error =
            getString(R.string.error_incorrect_password)
        is ConfirmPasswordRequiredAuthError -> confirmPasswordLayout.error =
            getString(R.string.error_field_required)
        is NotMatchedConfirmPasswordAuthError -> confirmPasswordLayout.error =
            getString(R.string.error_incorrect_password)
        else -> showFailRequestAlert(errorType, onRetry = ::sendChangePasswordRequest)
    }


    private val weakPasswordErrorMessage: String?
        get() = context?.let {
            val symbolsValue = "$MIN_PASSWORD_SIZE ${resources.getQuantityString(
                R.plurals.symbols,
                MIN_PASSWORD_SIZE
            )}"
            getString(R.string.weak_password_error, symbolsValue)
        }
}