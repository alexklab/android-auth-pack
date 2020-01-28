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

    object CanceledAuthError : ServiceAuthError("Request canceled")
    object WrongPasswordAuthError : ServiceAuthError("Wrong password")
    object EmailAlreadyExistAuthError : ServiceAuthError("Email already exist")
    object EmailVerificationAuthError : ServiceAuthError("Email verification required (Service side)")
    object AccountNotFoundAuthError : ServiceAuthError("Account not found")
    object LoginAlreadyExistAuthError : ServiceAuthError("Login already exist")
    object AccountNotActivatedAuthError : ServiceAuthError("Account not activated")
    object InvalidVerifyEmailCodeAuthError : ServiceAuthError("Invalid verify email code")
    object AccountsCollisionAuthError : ServiceAuthError("Accounts collision")
    object InvalidCredentialsAuthError : ServiceAuthError("Invalid credentials")
    object RecentLoginRequiredAuthError : ServiceAuthError("Recent login required")
    object TooManyRequestsAuthError : ServiceAuthError("Too many unsuccessful request attempts")

    /**
     * Errors that occurred at validation stage
     */
    open class VerificationAuthError(
        message: String,
        exception: Exception? = null,
        errorCode: Int = 0
    ) : AuthError(message, exception, errorCode)

    object EmailRequiredAuthError : VerificationAuthError("Email required")
    object LoginRequiredAuthError : VerificationAuthError("Login required")
    object PasswordRequiredAuthError : VerificationAuthError("Password required")
    object OldPasswordRequiredAuthError : VerificationAuthError("Old password required")
    object ConfirmPasswordRequiredAuthError : VerificationAuthError("Confirm password required")
    object WeakPasswordAuthError : VerificationAuthError("Weak password")
    object MalformedEmailAuthError : VerificationAuthError("Malformed email")
    object MalformedLoginAuthError : VerificationAuthError("Malformed login")
    object EnableTermsOfUseAuthError : VerificationAuthError("Enable terms of use required")
    object NotMatchedConfirmPasswordAuthError : VerificationAuthError("Not matched confirm password")
}