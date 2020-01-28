package com.demo.auth.firebase.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.android.arch.auth.core.data.entity.AuthError.*
import com.android.arch.auth.core.data.entity.AuthRequestStatus.*
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.EventObserver
import com.android.arch.auth.core.model.EditProfileViewModel
import com.demo.auth.firebase.R
import com.demo.auth.firebase.common.*
import com.demo.auth.firebase.data.entity.UserProfile
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import org.koin.android.ext.android.inject

class EditProfileFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        editProfileViewModel.apply{
            profile.observe(viewLifecycleOwner,  Observer(::updateProfileLayout))
            response.observe(viewLifecycleOwner, EventObserver(::handleEditProfileResponse))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginText.onFocusChangeListener = TextLayoutListener(loginLayout)
        emailText.onFocusChangeListener = TextLayoutListener(emailLayout)
        updateButton.setOnClickListener { sendEditProfileRequest() }
        //todo iconImageButton.setOnClickListener { cropIcon() }
    }


    override fun onResume() {
        super.onResume()
        progressBar.setGone()
    }

    private fun sendEditProfileRequest(): Unit = with(editProfileViewModel) {
        val userProfile = profile.value ?: return
        updateButton.isEnabled = false
        clearAllErrors(loginLayout, emailLayout)
        sendEditRequest {
            editEmail(userProfile.email, emailText.textValue())
            editLogin(userProfile.displayName, loginText.textValue())
        }
    }

    private fun handleEditProfileResponse(response: AuthResponse<UserProfile>): Unit = with(response) {
        Log.d("handleEditProfile", "$response")

        fun handleResponseError(): Unit = when (error) {
            LoginRequiredAuthError -> loginLayout.error = getString(R.string.error_field_required)
            MalformedLoginAuthError -> loginLayout.error = getString(R.string.login_validation_error)
            EmailRequiredAuthError -> emailLayout.error = getString(R.string.error_field_required)
            MalformedEmailAuthError -> emailLayout.error = getString(R.string.error_invalid_email)
            EmailAlreadyExistAuthError -> emailLayout.error = getString(R.string.error_email_in_use)
            // ask user to re-login and try send request again
            RecentLoginRequiredAuthError ->  emailLayout.error = getString(R.string.error_recent_login_required)
            else -> showFailRequestAlert(error, onRetry = ::sendEditProfileRequest)
        }

        fun dismissProgressAndHandleError() {
            progressBar.setGone()
            updateButton.isEnabled = true
            handleResponseError()
            Log.w("Fail AuthResponse", "$error", error?.exception)
        }

        when (status) {
            FAILED -> dismissProgressAndHandleError()
            SUCCESS -> activity?.onBackPressed()
            ON_PROGRESS -> progressBar.setVisible()
        }
    }

    private fun updateProfileLayout(profile: UserProfile?) {
        Log.d("profileObserver", "$profile")
        loginText.setText(profile?.displayName)
        emailText.setText(profile?.email)
        loadIcon(profile?.photoUrl, iconImageButton)
    }

    private val editProfileViewModel: EditProfileViewModel<UserProfile> by inject()
}