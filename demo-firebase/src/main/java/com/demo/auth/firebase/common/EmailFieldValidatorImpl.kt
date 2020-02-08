package com.demo.auth.firebase.common

import com.android.arch.auth.core.common.EmailFieldValidator
import java.util.regex.Pattern

class EmailFieldValidatorImpl : EmailFieldValidator {

    override fun validate(value: String): Boolean =
        value.length >= MIN_EMAIL_LENGTH &&
                EMAIL_PATTERN.matcher(value).find()

    private companion object {
        const val MIN_EMAIL_LENGTH = 5
        val EMAIL_PATTERN: Pattern = Pattern.compile(
            "[A-Z0-9a-z._+-]+@[A-Za-z0-9]+(\\.[A-Za-z]{2,})+",
            Pattern.CASE_INSENSITIVE
        )
    }
}