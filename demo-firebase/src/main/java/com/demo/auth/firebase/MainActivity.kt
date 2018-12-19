package com.demo.auth.firebase

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.demo.auth.firebase.common.applyTransaction
import com.demo.auth.firebase.data.database.DatabaseProvider
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
        supportFragmentManager.applyTransaction {
            replace(R.id.fragments_container, SignInFragment(), TAG)
        }
    }

    override fun onDestroy() {
        dbProvider.closeDb()
        super.onDestroy()
    }

    fun addFragment(fragment: Fragment) {
        supportFragmentManager.applyTransaction {
            replace(R.id.fragments_container, fragment, TAG)
                .addToBackStack(TAG)
        }
    }

    // lazy init dependency
    private val dbProvider: DatabaseProvider by inject()

    companion object {
        const val TAG = "main_activity"
    }
}