package com.android.arch.auth.core.data.entity

/**
 * Created by alexk on 11/21/18.
 * Project android-auth-pack
 */
enum class AuthResponseErrorType {
    // validations errors
    EMPTY_EMAIL,
    EMPTY_LOGIN,
    EMPTY_PASSWORD,
    EMPTY_NEW_PASSWORD,
    DISABLED_TERMS_OF_USE,
    EMPTY_CONFIRM_PASSWORD,

    INVALID_EMAIL,
    INVALID_LOGIN,
    INVALID_PASSWORD,
    INVALID_CONFIRM_PASSWORD,

    // Service response errors
    AUTH_CANCELED,
    AUTH_SERVICE_ERROR,
    AUTH_WRONG_PASSWORD,
    EMAIL_ALREADY_EXIST,
    EMAIL_VERIFICATION_REQUIRED,
    AUTH_ACCOUNT_NOT_FOUND,
    AUTH_LOGIN_ALREADY_EXIST,
    AUTH_ACCOUNT_NOT_ACTIVATED,
    AUTH_INVALID_VERIFY_EMAIL_CODE
}