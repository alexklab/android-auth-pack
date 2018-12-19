package com.demo.auth.firebase.data.network

import com.google.firebase.auth.AuthCredential
import java.lang.Exception

typealias NetworkSignInCallBack = (AuthCredential?, Exception?) -> Unit