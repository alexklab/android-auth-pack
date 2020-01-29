package com.demo.auth.google

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.demo.auth.google.common.applyTransaction
import com.demo.auth.google.common.viewModelProvider
import com.demo.auth.google.db.UserProfile
import com.demo.auth.google.repo.AuthRepository
import com.demo.auth.google.ui.SignInFragment
import com.demo.auth.google.ui.UserProfileFragment
import com.demo.auth.google.ui.UserProfileViewModel
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var repo: AuthRepository

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        repo.onCreate(this)
        viewModelProvider<UserProfileViewModel>(viewModelFactory).apply {
            profile.observe(this@MainActivity, Observer(::updateUI))
        }
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

    companion object {
        const val TAG = "main_activity"
    }
}
