package com.demo.auth.firebase.common

import com.android.arch.auth.core.common.FieldValidator

class PasswordFieldValidator : FieldValidator {

    override fun validate(value: String): Boolean =
        value.length >= MIN_PASSWORD_SIZE

    companion object {
        const val MIN_PASSWORD_SIZE = 6
    }
}