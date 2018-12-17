package com.android.arch.auth.core.common

interface FieldValidator {
    fun validate(value: String): Boolean
}