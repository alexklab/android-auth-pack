package com.demo.auth.firebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.android.arch.auth.firebase.FirebaseAuthRepository
import com.demo.auth.firebase.common.applyTransaction
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

    // lazy init dependency
    private val viewModel: UserProfileViewModel by inject()
    private val firebaseAuthRepository: FirebaseAuthRepository<UserProfile> by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseAuthRepository.onCreate(this)
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
        Log.d("MainActivity", "updateUI: $profile")
        replace(
            R.id.fragments_container,
            if (profile == null) {
                SignInFragment()
            } else {
                UserProfileFragment()
            }, TAG
        )
    }

    companion object {
        const val TAG = "main_activity"
    }
}