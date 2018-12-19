package com.demo.auth.firebase

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.demo.auth.firebase.common.applyTransaction
import com.demo.auth.firebase.data.database.DatabaseProvider
import com.demo.auth.firebase.data.network.FacebookSignInService
import com.demo.auth.firebase.data.network.GoogleSignInService
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
        dbProvider.openDb(this)
        googleSignInService.onCreate(this)
        facebookSignInService.onCreate(this)
        supportFragmentManager.applyTransaction {
            replace(R.id.fragments_container, SignInFragment(), TAG)
        }
    }

    override fun onDestroy() {
        dbProvider.closeDb()
        googleSignInService.onDestroy()
        facebookSignInService.onDestroy()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        googleSignInService.onActivityResult(requestCode, resultCode, data)
        facebookSignInService.onActivityResult(requestCode, resultCode, data)
    }

    fun addFragment(fragment: Fragment) {
        supportFragmentManager.applyTransaction {
            replace(R.id.fragments_container, fragment, TAG)
                .addToBackStack(TAG)
        }
    }

    // lazy init dependency
    private val dbProvider: DatabaseProvider by inject()
    private val googleSignInService: GoogleSignInService by inject()
    private val facebookSignInService: FacebookSignInService by inject()

    companion object {
        const val TAG = "main_activity"
    }
}