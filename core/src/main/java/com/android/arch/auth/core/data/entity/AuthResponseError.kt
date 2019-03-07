package com.android.arch.auth.core.data.entity
import java.lang.Exception

/**
 * Created by alexk on 11/21/18.
 * Project android-auth-pack
 *
 * Class hierarchies for authorisation errors
 */
sealed class AuthResponseError(
    val message: String,
    val exception: Exception? = null,
    val errorCode: Int? = null
) {

    /**
     * Errors that occurred at handle service response stage
     */
    open class ServiceError(
        message: String,
        exception: Exception? = null,
        errorCode: Int? = null
    ) : AuthResponseError( "Service Error: [${errorCode ?: "-"}]. $message", exception, errorCode)

    object Canceled : ServiceError("Canceled")
    object WrongPassword : ServiceError("Wrong password")
    object EmailAlreadyExist : ServiceError("Email already exist")
    object EmailVerificationRequired : ServiceError("Email verification required (Service side)")
    object AccountNotFound : ServiceError("Account not found")
    object LoginAlreadyExist : ServiceError("Login already exist")
    object AccountNotActivated : ServiceError("Account not activated")
    object InvalidVerifyEmailCode : ServiceError("Invalid verify email code")
    object AccountsCollision : ServiceError("Accounts collision")
    object InvalidCredentials : ServiceError("Invalid credentials")
    object RecentLoginRequired : ServiceError("Recent login required")

    /**
     * Errors that occurred at validation stage
     */
    open class VerificationError(
        message: String,
        exception: Exception? = null,
        errorCode: Int = 0
    ) : AuthResponseError( "Verification Error: $message", exception, errorCode)

    object EmailRequired: VerificationError( "Email required")
    object LoginRequired: VerificationError( "Login required")
    object PasswordRequired: VerificationError( "Password required")
    object OldPasswordRequired: VerificationError( "Old password required")
    object ConfirmPasswordRequired: VerificationError( "Confirm password required")
    object WeakPassword: VerificationError( "Weak password")
    object MalformedEmail: VerificationError( "Malformed email")
    object MalformedLogin: VerificationError( "Malformed login")
    object EnableTermsOfUseRequired: VerificationError( "Enable terms of use required")
    object NotMatchedConfirmPassword: VerificationError( "Not matched confirm password")

}