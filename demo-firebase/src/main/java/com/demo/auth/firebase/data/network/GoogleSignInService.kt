package com.demo.auth.firebase.data.network

interface GoogleSignInService {
    fun signIn(callback: GoogleSignInCallBack)
}