package com.demo.auth.firebase.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.auth.firebase.MainActivity
import com.demo.auth.firebase.R
import com.demo.auth.firebase.common.loadIcon
import com.demo.auth.firebase.common.setVisibleOrGone
import com.demo.auth.firebase.db.entity.UserProfile
import com.google.firebase.auth.EmailAuthProvider
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_user_profile.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class UserProfileFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val mainActivity: MainActivity? get() = activity as? MainActivity

    private val viewModel: UserProfileViewModel by activityViewModels { viewModelFactory }

    private val providersAdapter = UserInfoAdapter()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.profile.observe(viewLifecycleOwner, Observer(::updateUI))
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

        providersRecyclerView.apply {
            adapter = providersAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        editProfileButton.setOnClickListener { mainActivity?.addFragment(EditProfileFragment()) }
        changePasswordButton.setOnClickListener { mainActivity?.addFragment(ChangePasswordFragment()) }
        logoutButton.setOnClickListener { viewModel.logout() }
    }

    override fun onResume() {
        super.onResume()
        activity?.title = "Firebase: User profile"
        updateUI(viewModel.profile.value)
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

        val containsEmailAuthProvider = profile?.providersData
            ?.any { it.providerId == EmailAuthProvider.PROVIDER_ID }
            ?: false

        changePasswordButton.setVisibleOrGone(containsEmailAuthProvider)
        loadIcon(profile?.photoUrl, iconImageView)
        providersAdapter.updateAll(profile?.providersData.orEmpty())
    }

    private fun Long.toDateValue(): String = try {
        SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date(this))
    } catch (e: Exception) {
        Timber.e(e, "UserProfileFragment: Failed format Long.toDateValue")
        "Unformatted[$this]"
    }


    private companion object {
        const val DEFAULT_VALUE = "-NULL-"
        const val DATE_FORMAT = "dd MMMM yyyy HH:mm:ss"
    }
}