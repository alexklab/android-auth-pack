package com.android.arch.auth.core.data.entity

/**
 * Created by alexk on 11/21/18.
 * Project android-auth-pack
 *
 * Class hierarchies for authorisation errors
 */
sealed class AuthError(
    val message: String,
    val exception: Exception? = null
) {

    val errorName: String by lazy {
        val category = javaClass.superclass?.simpleName
            ?.replace("AuthError", "")
            ?: "AuthError"
        "$category.${javaClass.simpleName}"
    }

    override fun toString(): String {
        return "$errorName[$message]"
    }

    /**
     * Errors that occurred at handle service response stage
     */
    open class ServiceAuthError(
        message: String,
        exception: Exception? = null
    ) : AuthError(message, exception)

    class CanceledAuthError(e: Exception? = null) :
        ServiceAuthError(e?.message ?: "Request canceled", e)

    class WrongPasswordAuthError(e: Exception? = null) :
        ServiceAuthError(e?.message ?: "Wrong password")

    class EmailAlreadyExistAuthError(e: Exception? = null) :
        ServiceAuthError(e?.message ?: "Email already exist", e)

    class EmailVerificationAuthError(e: Exception? = null) :
        ServiceAuthError(e?.message ?: "Email verification required (Service side)", e)

    class AccountNotFoundAuthError(e: Exception? = null) :
        ServiceAuthError(e?.message ?: "Account not found", e)

    class LoginAlreadyExistAuthError(e: Exception? = null) :
        ServiceAuthError(e?.message ?: "Login already exist", e)

    class AccountNotActivatedAuthError(e: Exception? = null) :
        ServiceAuthError(e?.message ?: "Account not activated", e)

    class InvalidVerifyEmailCodeAuthError(e: Exception? = null) :
        ServiceAuthError(e?.message ?: "Invalid verify email code", e)

    class AccountsCollisionAuthError(e: Exception? = null) :
        ServiceAuthError(e?.message ?: "Accounts collision", e)

    class InvalidCredentialsAuthError(e: Exception? = null) :
        ServiceAuthError(e?.message ?: "Invalid credentials", e)

    class RecentLoginRequiredAuthError(e: Exception? = null) :
        ServiceAuthError(e?.message ?: "Recent login required", e)

    class TooManyRequestsAuthError(e: Exception? = null) :
        ServiceAuthError(e?.message ?: "Too many unsuccessful request attempts", e)

    /**
     * Errors that occurred at validation stage
     */
    open class VerificationAuthError(
        message: String,
        exception: Exception? = null
    ) : AuthError(message, exception)

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

    class WeakPasswordAuthError(e: Exception? = null) :
        VerificationAuthError(e?.message ?: "Weak password", e)

    class MalformedEmailAuthError(e: Exception? = null) :
        VerificationAuthError(e?.message ?: "Malformed email", e)

    class MalformedLoginAuthError :
        VerificationAuthError("Malformed login")

    class EnableTermsOfUseAuthError :
        VerificationAuthError("Enable terms of use required")

    class NotMatchedConfirmPasswordAuthError :
        VerificationAuthError("Not matched confirm password")
}