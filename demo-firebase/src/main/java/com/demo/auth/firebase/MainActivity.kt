package com.demo.auth.firebase

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.demo.auth.firebase.common.PasswordLayoutListener
import com.demo.auth.firebase.common.TextLayoutListener
import com.demo.auth.core.entity.AuthResponse
import com.demo.auth.core.entity.AuthResponseErrorType
import com.demo.auth.core.entity.EventObserver
import com.demo.auth.core.model.SignInWithEmailViewModel
import com.demo.auth.firebase.data.database.DatabaseProvider
import com.demo.auth.firebase.data.entity.UserProfile
import com.demo.auth.core.entity.AuthRequestStatus.*
import com.demo.auth.core.entity.AuthResponseErrorType.*
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject

/**
 * Created by alexk on 12/17/18.
 * Project android-auth-pack
 */

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbProvider.openDb(this)
        signInWithEmailViewModel.response.observe(this, responseEventObserver)

        email.onFocusChangeListener = TextLayoutListener(emailLayout)
        password.onFocusChangeListener = PasswordLayoutListener(passwordLayout)
        signInWithEmailButton.setOnClickListener { sendSignInWithEmailRequest() }
    }

    override fun onDestroy() {
        dbProvider.closeDb()
        super.onDestroy()
    }

    private fun handleSignInResponse(response: AuthResponse<UserProfile>): Unit = with(response) {
        Log.d("handleSignInResponse:", "update: $response")

        fun handleErrors(errorType: AuthResponseErrorType?) = when (errorType) {
            EMPTY_EMAIL -> emailLayout.error = getString(R.string.error_field_required)
            EMPTY_PASSWORD -> passwordLayout.error = getString(R.string.error_field_required)
            AUTH_CANCELED -> setButtonsClickable(isClickable = true)
            AUTH_WRONG_PASSWORD -> passwordLayout.error = getString(R.string.error_incorrect_password)
            AUTH_ACCOUNT_NOT_FOUND -> emailLayout.error = getString(R.string.error_email_not_found)
            AUTH_ACCOUNT_NOT_ACTIVATED -> emailLayout.error = getString(R.string.error_email_not_activated)
            else -> showFailRequestAlert(errorType, onRetry = ::sendSignInWithEmailRequest)
        }

        fun dismissProgressAndHandleError() {
            Log.w("handleSignInResponse:", "Fail response: $errorType. Cause: $errorMessage")
            Toast.makeText(this@MainActivity, "Error: $errorMessage", Toast.LENGTH_LONG).show()
            loginProgress.visibility = View.GONE
            setButtonsClickable(isClickable = true)
            handleErrors(errorType)
        }

        when (status) {
            FAILED -> dismissProgressAndHandleError()
            SUCCESS -> finish()
            ON_PROGRESS -> loginProgress.visibility = View.VISIBLE
        }
    }

    private fun sendSignInWithEmailRequest() {
        setButtonsClickable(isClickable = false)
        emailLayout.error = null
        passwordLayout.error = null
        signInWithEmailViewModel.signInWithEmail(email.textValue(), password.textValue())
    }

    private fun setButtonsClickable(isClickable: Boolean) {
        signInWithEmailButton.isClickable = isClickable
    }

    private fun showFailRequestAlert(errorType: AuthResponseErrorType?, onRetry: () -> Unit) {
        Log.w("showFailRequestAlert:", "Error: $errorType")
        AlertDialog.Builder(this)
            .setTitle(R.string.error_response_title)
            .setMessage(R.string.error_response_message)
            .setPositiveButton(R.string.retry) { _, _ -> onRetry() }
            .setNegativeButton(R.string.back) { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .create()
            .apply { setCanceledOnTouchOutside(true) }
            .show()
    }

    private fun EditText?.textValue(): String = this?.text?.toString().orEmpty()

    private val responseEventObserver = EventObserver(::handleSignInResponse)

    // lazy init dependency
    private val dbProvider: DatabaseProvider by inject()
    private val signInWithEmailViewModel: SignInWithEmailViewModel<UserProfile> by inject()
}