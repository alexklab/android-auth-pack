package com.demo.auth.google

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.demo.auth.google.common.applyTransaction
import com.demo.auth.google.database.DatabaseProvider
import com.demo.auth.google.entity.UserProfile
import com.demo.auth.google.repo.AuthRepository
import com.demo.auth.google.ui.SignInFragment
import com.demo.auth.google.ui.UserProfileFragment
import com.demo.auth.google.ui.UserProfileViewModel
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbProvider.onCreate(this)
        repo.onCreate(this)
        viewModel.profile.observe(this, Observer(::updateUI))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        repo.onActivityResult(requestCode, resultCode, data)
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

    private val repo: AuthRepository by inject()
    private val dbProvider: DatabaseProvider by inject()
    private val viewModel: UserProfileViewModel by inject()

    companion object {
        const val TAG = "main_activity"
    }
}
