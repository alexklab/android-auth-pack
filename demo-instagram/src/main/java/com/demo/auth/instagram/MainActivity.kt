package com.demo.auth.instagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.arch.instagram.InstagramSignInService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val service = InstagramSignInService(
            clientId = getString(R.string.instagram_client_id),
            redirectUrl = getString(R.string.instagram_redirect_url)
        )

        service.onCreate(this)

        signInButton.setOnClickListener {
            service.signIn { acc, _, e ->
                val msg = if (acc != null) {
                    "SignIn Success: $acc"
                } else {
                    "SignIn Error: ${e?.message}"
                }
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                Log.d("signIn Response", msg)
            }
        }
    }
}
