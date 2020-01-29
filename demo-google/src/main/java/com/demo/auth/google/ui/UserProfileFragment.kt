package com.demo.auth.google.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.demo.auth.google.R
import com.demo.auth.google.common.loadIcon
import com.demo.auth.google.common.viewModelProvider
import com.demo.auth.google.db.UserProfile
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_user_profile.*
import javax.inject.Inject

class UserProfileFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: UserProfileViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = viewModelProvider(viewModelFactory)
        viewModel.profile.observe(this@UserProfileFragment, Observer(::updateUI))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logoutButton.setOnClickListener { viewModel.logout() }
    }

    private fun updateUI(profile: UserProfile?) {
        emailTextView?.text = profile?.email ?: DEFAULT_VALUE
        accountIdTextView?.text = profile?.id ?: DEFAULT_VALUE
        displayNameTextView?.text = profile?.displayName ?: DEFAULT_VALUE
        givenNameTextView?.text = profile?.givenName ?: DEFAULT_VALUE
        familyNameTextView?.text = profile?.familyName ?: DEFAULT_VALUE
        loadIcon(profile?.photoUrl, iconImageView)
    }

    private companion object {
        const val DEFAULT_VALUE = "NULL"
    }
}