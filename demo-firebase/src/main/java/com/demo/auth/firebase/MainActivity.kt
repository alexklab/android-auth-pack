package com.demo.auth.firebase

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.demo.auth.firebase.common.applyTransaction
import com.demo.auth.firebase.data.database.DatabaseProvider
import com.demo.auth.firebase.data.entity.UserProfile
import com.android.arch.auth.facebook.FacebookSignInService
import com.android.arch.auth.google.GoogleSignInService
import com.android.arch.auth.twitter.TwitterSignInService
import com.demo.auth.firebase.data.repository.FirebaseAuthRepository
import com.demo.auth.firebase.data.ui.SignInFragment
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
            GoogleSignInService(
                webClientId = getString(R.string.google_web_client_id)
            ),
            TwitterSignInService(
                consumerApiKey = getString(R.string.twitter_consumer_api_key),
                consumerApiSecretKey = getString(R.string.twitter_consumer_api_secret_key)
            )
        )

        supportFragmentManager.applyTransaction {
            replace(R.id.fragments_container, SignInFragment(), TAG)
        }
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

    // lazy init dependency
    private val dbProvider: DatabaseProvider by inject()
    private val firebaseAuthRepository: FirebaseAuthRepository<UserProfile> by inject()

    companion object {
        const val TAG = "main_activity"
    }
}