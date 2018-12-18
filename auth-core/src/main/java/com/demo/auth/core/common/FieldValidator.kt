package com.demo.auth.core.common

interface FieldValidator {
    fun validate(value: String): Boolean
}