package com.android.arch.auth.core.data.entity

/**
 * Created by alexk on 11/21/18.
 * Project android-auth-pack
 *
 * Class hierarchies for authorisation errors
 */
sealed class AuthError(
    val message: String,
    val exception: Exception? = null,
    val errorCode: Int? = null
) {

    val errorName: String by lazy {
        val category = javaClass.superclass?.simpleName
            ?.replace("AuthError", "")
            ?: "AuthError"
        "$category.${javaClass.simpleName}"
    }

    override fun toString(): String {
        return "$errorName[${errorCode?.let { "$it, " } ?: ""}$message]"
    }

    /**
     * Errors that occurred at handle service response stage
     */
    open class ServiceAuthError(
        message: String,
        exception: Exception? = null,
        errorCode: Int? = null
    ) : AuthError(message, exception, errorCode)

    class CanceledAuthError : ServiceAuthError("Request canceled")

    class WrongPasswordAuthError(msg: String? = null) :
        ServiceAuthError(msg ?: "Wrong password")

    class EmailAlreadyExistAuthError(msg: String? = null) :
        ServiceAuthError(msg ?: "Email already exist")

    class EmailVerificationAuthError(msg: String?) :
        ServiceAuthError(msg ?: "Email verification required (Service side)")

    class AccountNotFoundAuthError(msg: String? = null) :
        ServiceAuthError(msg ?: "Account not found")

    class LoginAlreadyExistAuthError(msg: String? = null) :
        ServiceAuthError(msg ?: "Login already exist")

    class AccountNotActivatedAuthError(msg: String? = null) :
        ServiceAuthError(msg ?: "Account not activated")

    class InvalidVerifyEmailCodeAuthError(msg: String? = null) :
        ServiceAuthError(msg ?: "Invalid verify email code")

    class AccountsCollisionAuthError(msg: String? = null) :
        ServiceAuthError(msg ?: "Accounts collision")

    class InvalidCredentialsAuthError(msg: String? = null) :
        ServiceAuthError(msg ?: "Invalid credentials")

    class RecentLoginRequiredAuthError(msg: String? = null) :
        ServiceAuthError(msg ?: "Recent login required")

    class TooManyRequestsAuthError(msg: String? = null) :
        ServiceAuthError(msg ?: "Too many unsuccessful request attempts")

    /**
     * Errors that occurred at validation stage
     */
    open class VerificationAuthError(
        message: String,
        exception: Exception? = null,
        errorCode: Int = 0
    ) : AuthError(message, exception, errorCode)

    class EmailRequiredAuthError :
        VerificationAuthError("Email required")

    class LoginRequiredAuthError :
        VerificationAuthError("Login required")

    class PasswordRequiredAuthError :
        VerificationAuthError("Password required")

    class OldPasswordRequiredAuthError :
        VerificationAuthError("Old password required")

    class ConfirmPasswordRequiredAuthError :
        VerificationAuthError("Confirm password required")

    class WeakPasswordAuthError(msg: String? = null) :
        VerificationAuthError(msg ?: "Weak password")

    class MalformedEmailAuthError(msg: String? = null) :
        VerificationAuthError(msg ?: "Malformed email")

    class MalformedLoginAuthError :
        VerificationAuthError("Malformed login")

    class EnableTermsOfUseAuthError :
        VerificationAuthError("Enable terms of use required")

    class NotMatchedConfirmPasswordAuthError :
        VerificationAuthError("Not matched confirm password")
}