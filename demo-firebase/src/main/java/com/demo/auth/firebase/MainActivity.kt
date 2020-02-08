package com.demo.auth.firebase

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.arch.auth.firebase.FirebaseAuthRepository
import com.demo.auth.firebase.common.applyTransaction
import com.demo.auth.firebase.common.viewModelProvider
import com.demo.auth.firebase.db.entity.UserProfile
import com.demo.auth.firebase.ui.UserProfileFragment
import com.demo.auth.firebase.ui.UserProfileViewModel
import com.demo.auth.firebase.ui.signin.SignInFragment
import dagger.android.support.DaggerAppCompatActivity
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by alexk on 12/17/18.
 * Project android-auth-pack
 */

class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var firebaseAuthRepository: FirebaseAuthRepository<UserProfile>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseAuthRepository.onCreate(this)
        viewModelProvider<UserProfileViewModel>(viewModelFactory).apply {
            profile.observe(this@MainActivity, Observer(::updateUI))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("request=$requestCode, result=$resultCode, data=${data?.extras}")
        firebaseAuthRepository.onActivityResult(requestCode, resultCode, data)
    }

    fun addFragment(fragment: Fragment) {
        Timber.d("next fragment: $fragment")
        supportFragmentManager.applyTransaction {
            replace(R.id.fragments_container, fragment, TAG)
                .addToBackStack(TAG)
        }
    }

    private fun updateUI(profile: UserProfile?): Unit = supportFragmentManager.applyTransaction {
        Timber.d("updateUI: $profile")
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