package com.demo.auth.firebase.data.network

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import java.lang.Exception

typealias GoogleSignInCallBack = (GoogleSignInAccount?, Exception?) -> Unit