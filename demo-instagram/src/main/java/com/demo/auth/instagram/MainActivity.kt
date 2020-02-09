package com.demo.auth.instagram

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.arch.auth.core.data.entity.SignInResponse
import com.android.arch.auth.core.data.entity.SocialNetworkType
import com.android.arch.auth.core.data.network.SignInServiceListener
import com.android.arch.instagram.InstagramSignInService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SignInServiceListener {

    private lateinit var instagramSignInService: InstagramSignInService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        instagramSignInService = InstagramSignInService(
            clientId = getString(R.string.instagram_client_id),
            redirectUrl = getString(R.string.instagram_redirect_url)
        ).also {
            it.onCreate(this@MainActivity)
            it.addListener(this@MainActivity)
        }

        signInButton.setOnClickListener { instagramSignInService.signIn() }
    }

    override fun onDestroy() {
        super.onDestroy()
        instagramSignInService.removeListener(this@MainActivity)
    }

    override fun onSignInResponse(socialNetwork: SocialNetworkType, response: SignInResponse) {
        val (profile, error) = response
        val msg = if (profile != null) "Success: $profile" else "Error: $error"
        Log.d("MainActivity", "onSignInResponse: $msg", error?.exception)
        Toast
            .makeText(this@MainActivity, "SignIn Response: $msg", Toast.LENGTH_LONG)
            .show()
    }
}
