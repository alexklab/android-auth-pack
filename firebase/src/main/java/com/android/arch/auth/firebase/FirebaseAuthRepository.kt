package com.android.arch.auth.firebase

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.common.extensions.postError
import com.android.arch.auth.core.common.extensions.postEvent
import com.android.arch.auth.core.data.entity.*
import com.android.arch.auth.core.data.entity.AuthRequestStatus.FAILED
import com.android.arch.auth.core.data.entity.AuthRequestStatus.SUCCESS
import com.android.arch.auth.core.data.entity.AuthResponseErrorType.*
import com.android.arch.auth.core.data.entity.SocialNetworkType.*
import com.android.arch.auth.core.data.repository.EmailAuthRepository
import com.android.arch.auth.core.data.repository.NetworkAuthRepository
import com.android.arch.auth.core.data.repository.SocialNetworkAuthRepository
import com.google.firebase.auth.*

class FirebaseAuthRepository<UserProfileDataType>(
    private val userDataFactory: Factory<UserProfileDataType>
) : EmailAuthRepository<UserProfileDataType>,
    SocialNetworkAuthRepository<UserProfileDataType>,
    NetworkAuthRepository() {

    interface Factory<UserProfileDataType> {
        fun create(user: FirebaseUser): UserProfileDataType
    }

    override fun signOut() {
        auth.signOut()
        allServicesSignOut()
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
            ?.addOnCompleteListener { response.postResult(it.isSuccessful, it.exception, ::updatePasswordErrors) }
            ?: response.postError(AUTH_RECENT_LOGIN_REQUIRED, "Fail updatePassword: user is logged out")
    }

    override fun recoverPassword(
        email: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                response.postResult(
                    it.isSuccessful,
                    it.exception,
                    ::sendPasswordResetEmailErrors
                )
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
                if (it.isSuccessful) auth.currentUser?.sendEmailVerification()
                response.postResult(it.isSuccessful, it.exception, ::signUpWithEmailErrors)
            }
    }

    override fun signInWithEmail(
        email: String,
        password: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { response.postResult(it.isSuccessful, it.exception, ::signInWithEmailErrors) }
    }

    override fun signInWithSocialNetwork(
        socialNetwork: SocialNetworkType,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        getService(socialNetwork)?.apply {
            signIn { response.signInWithCredential(socialNetwork, it) }
        } ?: response.postError(AUTH_CANCELED, "Fail signInWith $socialNetwork: undefined service")
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
            updateProfile(request)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        updateEmail(email).addOnCompleteListener {
                            response.postResult(
                                it.isSuccessful,
                                it.exception,
                                ::updateEmailErrors
                            )
                        }
                    } else {
                        response.postResult(task.isSuccessful, task.exception, ::updateProfileErrors)
                    }
                }
        } ?: response.postError(AUTH_RECENT_LOGIN_REQUIRED, "Fail updateProfile: user is logged out")
    }

    override fun sendVerifiedEmailKeyUseCase(
        verifyKey: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        // implemented on firebase back side
        response.postError(AUTH_SERVICE_ERROR)
    }

    private fun MutableLiveData<Event<AuthResponse<UserProfileDataType>>>.signInWithCredential(
        socialNetwork: SocialNetworkType,
        response: SignInResponse
    ) {
        getAuthCredential(socialNetwork, response)?.let { credential ->
            auth.signInWithCredential(credential)
                .addOnCompleteListener { postResult(it.isSuccessful, it.exception, ::signInWithCredentialsErrors) }
        } ?: postError(response.errorType ?: AUTH_CANCELED, response.exception?.message)

    }

    private fun signInWithCredentialsErrors(exception: Exception?): AuthResponseErrorType? = when (exception) {
        // thrown if the user account you are trying to sign in to has been disabled.
        // Also thrown if credential is an EmailAuthCredential with an email address that does not correspond to an existing user.
        is FirebaseAuthInvalidUserException -> AUTH_ACCOUNT_NOT_FOUND
        // thrown if the credential is malformed or has expired.
        // If credential instanceof EmailAuthCredential it will be thrown if the password is incorrect.
        is FirebaseAuthInvalidCredentialsException -> AUTH_INVALID_CREDENTIALS
        // thrown if there already exists an account with the email address asserted by the credential.
        // Resolve this case by calling fetchProvidersForEmail(String) and then asking the user to sign in using one of them.
        is FirebaseAuthUserCollisionException -> AUTH_USER_COLLISION
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
        is FirebaseAuthWeakPasswordException -> WEAK_PASSWORD
        // thrown if the email address is malformed
        is FirebaseAuthInvalidCredentialsException -> MALFORMED_EMAIL
        // thrown if there already exists an account with the given email address
        is FirebaseAuthUserCollisionException -> AUTH_EMAIL_ALREADY_EXIST
        else -> AUTH_SERVICE_ERROR
    }

    private fun sendPasswordResetEmailErrors(exception: Exception?): AuthResponseErrorType? = when (exception) {
        // thrown when given an ActionCodeSettings that does not have canHandleCodeInApp set to true.
        is IllegalArgumentException -> AUTH_SERVICE_ERROR
        else -> AUTH_SERVICE_ERROR
    }

    private fun updateProfileErrors(exception: Exception?): AuthResponseErrorType? = when (exception) {
        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AUTH_ACCOUNT_NOT_FOUND
        else -> AUTH_SERVICE_ERROR
    }

    private fun updatePasswordErrors(exception: Exception?): AuthResponseErrorType? = when (exception) {
        // thrown if the password is not strong enough
        is FirebaseAuthWeakPasswordException -> WEAK_PASSWORD
        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AUTH_ACCOUNT_NOT_FOUND
        // thrown if the user's last sign-in time does not meet the security threshold.
        is FirebaseAuthRecentLoginRequiredException -> AUTH_RECENT_LOGIN_REQUIRED
        else -> AUTH_SERVICE_ERROR
    }

    private fun updateEmailErrors(exception: Exception?): AuthResponseErrorType? = when (exception) {
        // thrown if the email address is malformed
        is FirebaseAuthInvalidCredentialsException -> MALFORMED_EMAIL
        // thrown if there already exists an account with the given email address
        is FirebaseAuthUserCollisionException -> AUTH_EMAIL_ALREADY_EXIST
        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AUTH_ACCOUNT_NOT_FOUND
        // thrown if the user's last sign-in time does not meet the security threshold.
        // Use reauthenticate(AuthCredential) to resolve. This does not apply if the user is anonymous.
        is FirebaseAuthRecentLoginRequiredException -> AUTH_RECENT_LOGIN_REQUIRED
        else -> AUTH_SERVICE_ERROR
    }

    private fun MutableLiveData<Event<AuthResponse<UserProfileDataType>>>.postResult(
        isSuccessful: Boolean,
        exception: Exception?,
        handleErrors: (Exception?) -> AuthResponseErrorType?
    ) {
        postEvent(AuthResponse(
            status = if (isSuccessful) SUCCESS else FAILED,
            errorType = if (isSuccessful) null else handleErrors(exception),
            errorMessage = exception?.message,
            data = auth.currentUser?.let { userDataFactory.create(it) }
        ))
    }

    private fun getAuthCredential(socialNetwork: SocialNetworkType, response: SignInResponse): AuthCredential? =
        with(response) {
            val responseToken = token
            if (responseToken == null) {
                Log.e("getAuthCredential", "Undefined token. Provider $socialNetwork")
                return null
            }
            when (socialNetwork) {
                GOOGLE -> GoogleAuthProvider.getCredential(responseToken, null)
                FACEBOOK -> FacebookAuthProvider.getCredential(responseToken)
                TWITTER -> {
                    val responseTokenSecret = tokenSecret
                    if (responseTokenSecret == null) {
                        Log.e("getAuthCredential", "Undefined tokenSecret. Provider $socialNetwork")
                        return null
                    }
                    TwitterAuthProvider.getCredential(responseToken, responseTokenSecret)
                }
                else -> null
            }
        }

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
}