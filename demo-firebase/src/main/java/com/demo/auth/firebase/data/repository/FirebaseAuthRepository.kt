package com.demo.auth.firebase.data.repository

import androidx.lifecycle.MutableLiveData
import com.demo.auth.core.common.extensions.postError
import com.demo.auth.core.common.extensions.postEvent
import com.demo.auth.core.entity.*
import com.demo.auth.core.entity.AuthRequestStatus.*
import com.demo.auth.core.entity.AuthResponseErrorType.*
import com.demo.auth.core.repos.AuthRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*

class FirebaseAuthRepository<UserProfileDataType>(
    private val userDataFactory: Factory<UserProfileDataType>
) : AuthRepository<UserProfileDataType> {

    interface Factory<UserProfileDataType> {
        fun create(user: FirebaseUser): UserProfileDataType
    }

    /**
     * this is a security sensitive operation that requires the user to have recently signed in.
     */
    override fun changePassword(
        uid: String,
        oldPassword: String,
        newPassword: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        auth.currentUser
            ?.updatePassword(newPassword)
            ?.addOnCompleteListener {
                response.postEvent(
                    AuthResponse(
                        status = it.authRequestStatus,
                        errorType = if (it.isSuccessful) {
                            null
                        } else {
                            when (it.exception) {
                                // thrown if the password is not strong enough
                                is FirebaseAuthWeakPasswordException,
                                    // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
                                is FirebaseAuthInvalidUserException,
                                    // thrown if the user's last sign-in time does not meet the security threshold.
                                is FirebaseAuthRecentLoginRequiredException -> INVALID_CONFIRM_PASSWORD
                                else -> AUTH_SERVICE_ERROR
                            }
                        }
                    )
                )
            }
            ?: response.postError(AUTH_SERVICE_ERROR)
    }

    override fun recoverPassword(
        email: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                response.postEvent(AuthResponse(it.authRequestStatus, it.authResponseErrorType))
            }
    }

    override fun signUp(
        login: String,
        email: String,
        password: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    auth.currentUser?.sendEmailVerification()
                }
                response.postEvent(it.toAuthResponse(::handleSignUpWithEmailErrors))
            }
    }

    override fun signInWithEmail(
        email: String,
        password: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                response.postEvent(it.toAuthResponse(::handleSignInWithEmailErrors))
            }
    }

    override fun signInWithSocialNetwork(
        socialNetwork: SocialNetworkType,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        response.postError(AUTH_CANCELED)
        // TODO: "not implemented"
    }

    /**
     * this is a security sensitive operation that requires the user to have recently signed in.
     */
    override fun sendUpdateProfileRequest(
        uid: String,
        login: String,
        email: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        auth.currentUser?.apply {
            val request = UserProfileChangeRequest.Builder()
                .setDisplayName(login)
                .build()
            updateProfile(request).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateEmail(email).addOnCompleteListener {
                        response.postEvent(AuthResponse(it.authRequestStatus, it.authResponseErrorType))
                    }
                } else {
                    response.postError(AUTH_SERVICE_ERROR)
                }
            }
        } ?: response.postError(AUTH_SERVICE_ERROR)
    }

    override fun sendVerifiedEmailKeyUseCase(
        verifyKey: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        // implemented on firebase back side
        response.postError(AUTH_SERVICE_ERROR)
    }

    private fun handleSignInWithEmailErrors(task: Task<AuthResult>): AuthResponseErrorType? =
        if (task.isSuccessful) {
            null
        } else {
            when (task.exception) {
                // thrown if the user account corresponding to email does not exist or has been disabled
                is FirebaseAuthInvalidUserException -> AUTH_ACCOUNT_NOT_FOUND
                // thrown if the password is wrong
                is FirebaseAuthInvalidCredentialsException -> AUTH_WRONG_PASSWORD
                else -> AUTH_SERVICE_ERROR
            }
        }

    private fun handleSignUpWithEmailErrors(task: Task<AuthResult>): AuthResponseErrorType? =
        if (task.isSuccessful) {
            null
        } else {
            when (task.exception) {
                // thrown if the password is not strong enough
                is FirebaseAuthWeakPasswordException -> INVALID_PASSWORD
                // thrown if the email address is malformed
                is FirebaseAuthInvalidCredentialsException -> INVALID_EMAIL
                // thrown if there already exists an account with the given email address
                is FirebaseAuthUserCollisionException -> EMAIL_ALREADY_EXIST
                else -> AUTH_SERVICE_ERROR
            }
        }

    private fun Task<AuthResult>.toAuthResponse(handleErrors: (Task<AuthResult>) -> AuthResponseErrorType?) =
        AuthResponse(
            status = authRequestStatus,
            errorType = handleErrors(this),
            errorMessage = exception?.message,
            data = auth.currentUser?.let { userDataFactory.create(it) }
        )

    private val Task<*>.authResponseErrorType: AuthResponseErrorType?
        get() = if (isSuccessful) null else AUTH_SERVICE_ERROR

    private val Task<*>.authRequestStatus: AuthRequestStatus
        get() = if (isSuccessful) SUCCESS else FAILED

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
}