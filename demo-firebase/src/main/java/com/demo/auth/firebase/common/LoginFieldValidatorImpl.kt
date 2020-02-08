package com.demo.auth.firebase.common

import com.android.arch.auth.core.common.LoginFieldValidator

class LoginFieldValidatorImpl : LoginFieldValidator {

    override fun validate(value: String) = value.length >= LOGIN_MIN_LENGTH

    companion object {
        private const val LOGIN_MIN_LENGTH = 3
    }
}