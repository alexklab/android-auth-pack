package com.demo.auth.firebase.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.auth.firebase.R
import com.demo.auth.firebase.common.loadIcon
import com.demo.auth.firebase.data.entity.UserProfile
import kotlinx.android.synthetic.main.fragment_user_profile.*
import org.koin.android.ext.android.inject
import java.util.*

class UserProfileFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.profile.observe(this, Observer(::updateUI))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        providersRecyclerView.apply {
            adapter = providersAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        logoutButton.setOnClickListener { viewModel.logout() }
    }

    private fun updateUI(profile: UserProfile?) {
        uidTextView?.text = profile?.uid ?: DEFAULT_VALUE
        providerTextView?.text = profile?.providerId ?: DEFAULT_VALUE
        creationTextView?.text = profile?.creationTimestamp?.toDateValue() ?: DEFAULT_VALUE
        lastSignInTextView?.text = profile?.lastSignInTimestamp?.toDateValue() ?: DEFAULT_VALUE
        nameTextView?.text = profile?.displayName ?: DEFAULT_VALUE
        phoneTextView?.text = profile?.phoneNumber ?: DEFAULT_VALUE
        emailTextView?.text = profile?.email ?: DEFAULT_VALUE
        emailVerifyTextView?.text = profile?.isEmailVerified?.toString() ?: DEFAULT_VALUE
        loadIcon(profile?.photoUrl, iconImageView)
        providersAdapter.updateAll(profile?.providersData.orEmpty())
    }

    private fun Long.toDateValue(): String {
        val date = Date(this * 1000)
        return date.toString()
    }

    private val providersAdapter = UserInfoAdapter()
    private val viewModel: UserProfileViewModel by inject()

    companion object {
        private const val DEFAULT_VALUE = "-NULL-"
    }
}