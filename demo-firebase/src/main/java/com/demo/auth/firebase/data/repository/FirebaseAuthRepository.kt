package com.demo.auth.firebase.data.repository

import androidx.lifecycle.MutableLiveData
import com.demo.auth.core.common.extensions.postError
import com.demo.auth.core.common.extensions.postEvent
import com.demo.auth.core.entity.*
import com.demo.auth.core.entity.AuthRequestStatus.FAILED
import com.demo.auth.core.entity.AuthRequestStatus.SUCCESS
import com.demo.auth.core.entity.AuthResponseErrorType.*
import com.demo.auth.core.entity.SocialNetworkType.*
import com.demo.auth.core.repos.AuthRepository
import com.demo.auth.firebase.data.network.NetworkSignInService
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*

class FirebaseAuthRepository<UserProfileDataType>(
    private val userDataFactory: Factory<UserProfileDataType>,
    private val googleSignInService: NetworkSignInService,
    private val facebookSignInService: NetworkSignInService
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
            ?.addOnCompleteListener { response.postResult(it, ::updatePasswordErrors) }
            ?: response.postError(AUTH_SERVICE_ERROR, "Fail updatePassword: user is logged out")
    }

    override fun recoverPassword(
        email: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { response.postResult(it, ::sendPasswordResetEmailErrors) }
    }

    override fun signUp(
        login: String,
        email: String,
        password: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) auth.currentUser?.sendEmailVerification()
                response.postResult(it, ::signUpWithEmailErrors)
            }
    }

    override fun signInWithEmail(
        email: String,
        password: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { response.postResult(it, ::signInWithEmailErrors) }
    }

    override fun signInWithSocialNetwork(
        socialNetwork: SocialNetworkType,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        when (socialNetwork) {
            GOOGLE -> googleSignInService.signIn { c, e -> response.signInWithCredential(c, e) }
            FACEBOOK -> facebookSignInService.signIn { c, e -> response.signInWithCredential(c, e) }
            else -> response.postError(AUTH_CANCELED, "$socialNetwork signInService not implemented")
        }
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
                    updateEmail(email).addOnCompleteListener { response.postResult(it, ::updateEmailErrors) }
                } else {
                    response.postResult(task, ::updateProfileErrors)
                }
            }
        } ?: response.postError(AUTH_SERVICE_ERROR, "Fail updateProfile: user is logged out")
    }

    override fun sendVerifiedEmailKeyUseCase(
        verifyKey: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        // implemented on firebase back side
        response.postError(AUTH_SERVICE_ERROR)
    }

    private fun MutableLiveData<Event<AuthResponse<UserProfileDataType>>>.signInWithCredential(
        credential: AuthCredential?,
        exception: Exception?
    ) {
        if (credential != null) {
            auth.signInWithCredential(credential)
                .addOnCompleteListener { postResult(it, ::signInWithCredentialsErrors) }
        } else {
            postError(AUTH_CANCELED, exception?.message)
        }
    }

    private fun signInWithCredentialsErrors(exception: Exception?): AuthResponseErrorType? = when (exception) {

        // thrown if the user account you are trying to sign in to has been disabled.
        // Also thrown if credential is an EmailAuthCredential with an email address that does not correspond to an existing user.
        is FirebaseAuthInvalidUserException,

        // thrown if the credential is malformed or has expired.
        // If credential instanceof EmailAuthCredential it will be thrown if the password is incorrect.
        is FirebaseAuthInvalidCredentialsException,

        // thrown if there already exists an account with the email address asserted by the credential.
        // Resolve this case by calling fetchProvidersForEmail(String) and then asking the user to sign in using one of them.
        is FirebaseAuthUserCollisionException -> AUTH_SERVICE_ERROR

        else -> AUTH_SERVICE_ERROR
    }


    private fun signInWithEmailErrors(exception: Exception?): AuthResponseErrorType? = when (exception) {
        // thrown if the user account corresponding to email does not exist or has been disabled
        is FirebaseAuthInvalidUserException -> AUTH_ACCOUNT_NOT_FOUND
        // thrown if the password is wrong
        is FirebaseAuthInvalidCredentialsException -> AUTH_WRONG_PASSWORD
        else -> AUTH_SERVICE_ERROR

    }

    private fun signUpWithEmailErrors(exception: Exception?): AuthResponseErrorType? = when (exception) {
        // thrown if the password is not strong enough
        is FirebaseAuthWeakPasswordException -> INVALID_PASSWORD
        // thrown if the email address is malformed
        is FirebaseAuthInvalidCredentialsException -> INVALID_EMAIL
        // thrown if there already exists an account with the given email address
        is FirebaseAuthUserCollisionException -> EMAIL_ALREADY_EXIST
        else -> AUTH_SERVICE_ERROR
    }

    private fun sendPasswordResetEmailErrors(exception: Exception?): AuthResponseErrorType? = when (exception) {
        // thrown when given an ActionCodeSettings that does not have canHandleCodeInApp set to true.
        is IllegalArgumentException -> AUTH_SERVICE_ERROR
        else -> AUTH_SERVICE_ERROR
    }

    private fun updateProfileErrors(exception: Exception?): AuthResponseErrorType? = when (exception) {
        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AUTH_SERVICE_ERROR
        else -> AUTH_SERVICE_ERROR
    }

    private fun updatePasswordErrors(exception: Exception?): AuthResponseErrorType? = when (exception) {
        // thrown if the password is not strong enough
        is FirebaseAuthWeakPasswordException,
            // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException,
            // thrown if the user's last sign-in time does not meet the security threshold.
        is FirebaseAuthRecentLoginRequiredException -> INVALID_CONFIRM_PASSWORD
        else -> AUTH_SERVICE_ERROR
    }

    private fun updateEmailErrors(exception: Exception?): AuthResponseErrorType? = when (exception) {
        // thrown if the email address is malformed
        is FirebaseAuthInvalidCredentialsException -> INVALID_EMAIL

        // thrown if there already exists an account with the given email address
        is FirebaseAuthUserCollisionException -> EMAIL_ALREADY_EXIST

        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AUTH_SERVICE_ERROR

        // thrown if the user's last sign-in time does not meet the security threshold.
        // Use reauthenticate(AuthCredential) to resolve. This does not apply if the user is anonymous.
        is FirebaseAuthRecentLoginRequiredException -> AUTH_SERVICE_ERROR

        else -> AUTH_SERVICE_ERROR
    }

    private fun MutableLiveData<Event<AuthResponse<UserProfileDataType>>>.postResult(
        task: Task<*>,
        handleErrors: (Exception?) -> AuthResponseErrorType?
    ) {
        postEvent(AuthResponse(
            status = task.authRequestStatus,
            errorType = if (task.isSuccessful) null else handleErrors(task.exception),
            errorMessage = task.exception?.message,
            data = auth.currentUser?.let { userDataFactory.create(it) }
        ))
    }

    private val Task<*>.authRequestStatus: AuthRequestStatus
        get() = if (isSuccessful) SUCCESS else FAILED

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
}