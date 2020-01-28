package com.demo.auth.firebase

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.android.arch.auth.facebook.FacebookSignInService
import com.android.arch.auth.firebase.FirebaseAuthRepository
import com.android.arch.auth.google.GoogleSignInService
import com.android.arch.auth.twitter.TwitterSignInService
import com.demo.auth.firebase.common.applyTransaction
import com.demo.auth.firebase.data.database.DatabaseProvider
import com.demo.auth.firebase.data.entity.UserProfile
import com.demo.auth.firebase.ui.SignInFragment
import com.demo.auth.firebase.ui.UserProfileFragment
import com.demo.auth.firebase.ui.UserProfileViewModel
import org.koin.android.ext.android.inject

/**
 * Created by alexk on 12/17/18.
 * Project android-auth-pack
 */

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbProvider.onCreate(this)
        firebaseAuthRepository.onCreate(
            this,
            FacebookSignInService(),
            GoogleSignInService(webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID),
            TwitterSignInService(
                consumerApiKey = BuildConfig.TWITTER_CONSUMER_API_KEY,
                consumerApiSecretKey = BuildConfig.TWITTER_CONSUMER_API_SECRET_KEY
            )
        )

        viewModel.profile.observe(this, Observer(::updateUI))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        firebaseAuthRepository.onActivityResult(requestCode, resultCode, data)
    }

    fun addFragment(fragment: Fragment) {
        supportFragmentManager.applyTransaction {
            replace(R.id.fragments_container, fragment, TAG)
                .addToBackStack(TAG)
        }
    }

    private fun updateUI(profile: UserProfile?): Unit = supportFragmentManager.applyTransaction {
        replace(
            R.id.fragments_container,
            if (profile == null) {
                SignInFragment()
            } else {
                UserProfileFragment()
            }, TAG
        )
    }

    // lazy init dependency
    private val dbProvider: DatabaseProvider by inject()
    private val viewModel: UserProfileViewModel by inject()
    private val firebaseAuthRepository: FirebaseAuthRepository<UserProfile> by inject()

    companion object {
        const val TAG = "main_activity"
    }
}