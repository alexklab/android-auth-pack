package com.demo.auth.firebase

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.demo.auth.firebase.common.applyTransaction
import com.demo.auth.firebase.data.database.DatabaseProvider
import com.demo.auth.firebase.data.entity.UserProfile
import com.demo.auth.firebase.data.network.FacebookSignInService
import com.demo.auth.firebase.data.network.GoogleSignInService
import com.demo.auth.firebase.data.network.TwitterSignInService
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
            GoogleSignInService(),
            TwitterSignInService()
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