package com.android.arch.auth.firebase

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.common.extensions.postError
import com.android.arch.auth.core.common.extensions.postEvent
import com.android.arch.auth.core.data.entity.*
import com.android.arch.auth.core.data.entity.AuthRequestStatus.FAILED
import com.android.arch.auth.core.data.entity.AuthRequestStatus.SUCCESS
import com.android.arch.auth.core.data.entity.AuthResponseError.*
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
            ?.addOnCompleteListener { response.postResult(it.isSuccessful, it.exception?.toUpdatePasswordError()) }
            ?: response.postError(RecentLoginRequired)
    }

    override fun recoverPassword(
        email: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                response.postResult(it.isSuccessful, it.exception?.toSendPasswordResetEmailError())
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
                response.postResult(it.isSuccessful, it.exception?.toSignUpWithEmailError())
            }
    }

    override fun signInWithEmail(
        email: String,
        password: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { response.postResult(it.isSuccessful, it.exception?.toSignInWithEmailError()) }
    }

    override fun signInWithSocialNetwork(
        socialNetwork: SocialNetworkType,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        getService(socialNetwork)?.apply {
            signIn { response.signInWithCredential(socialNetwork, it) }
        } ?: response.postError(AuthResponseError.Canceled)
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
                            response.postResult(it.isSuccessful, it.exception?.toUpdateEmailError())
                        }
                    } else {
                        response.postResult(task.isSuccessful, task.exception?.toUpdateProfileError())
                    }
                }
        } ?: response.postError(RecentLoginRequired)
    }

    override fun sendVerifiedEmailKeyUseCase(
        verifyKey: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        // implemented on firebase back side
        response.postError(ServiceError("Unsupported method usage. Actually implemented on Firebase back side"))
    }

    private fun MutableLiveData<Event<AuthResponse<UserProfileDataType>>>.signInWithCredential(
        socialNetwork: SocialNetworkType,
        response: SignInResponse
    ) {
        getAuthCredential(socialNetwork, response)?.let { credential ->
            auth.signInWithCredential(credential)
                .addOnCompleteListener { postResult(it.isSuccessful, it.exception?.toSignInWithCredentialsError()) }
        } ?: postError(response.error ?: Canceled)

    }

    private fun Exception.toSignInWithCredentialsError(): AuthResponseError? = when (this) {
        // thrown if the user account you are trying to sign in to has been disabled.
        // Also thrown if credential is an EmailAuthCredential with an email address that does not correspond to an existing user.
        is FirebaseAuthInvalidUserException -> AccountNotFound
        // thrown if the credential is malformed or has expired.
        // If credential instanceof EmailAuthCredential it will be thrown if the password is incorrect.
        is FirebaseAuthInvalidCredentialsException -> InvalidCredentials
        // thrown if there already exists an account with the email address asserted by the credential.
        // Resolve this case by calling fetchProvidersForEmail(String) and then asking the user to sign in using one of them.
        is FirebaseAuthUserCollisionException -> AccountsCollision
        else -> ServiceError("Firebase: Sign in with credentials failed", this)
    }


    private fun Exception.toSignInWithEmailError(): AuthResponseError? = when (this) {
        // thrown if the user account corresponding to email does not exist or has been disabled
        is FirebaseAuthInvalidUserException -> AccountNotFound
        // thrown if the password is wrong
        is FirebaseAuthInvalidCredentialsException -> AuthResponseError.WrongPassword
        else -> ServiceError("Firebase: Sign in with email failed", this)
    }

    private fun Exception.toSignUpWithEmailError(): AuthResponseError? = when (this) {
        // thrown if the password is not strong enough
        is FirebaseAuthWeakPasswordException -> WeakPassword
        // thrown if the email address is malformed
        is FirebaseAuthInvalidCredentialsException -> MalformedEmail
        // thrown if there already exists an account with the given email address
        is FirebaseAuthUserCollisionException -> EmailAlreadyExist
        else -> ServiceError("Firebase: Sign up with email failed", this)
    }

    private fun Exception.toSendPasswordResetEmailError(): AuthResponseError? = when (this) {
        // thrown when given an ActionCodeSettings that does not have canHandleCodeInApp set to true.
        is IllegalArgumentException -> ServiceError("Firebase: Sending reset email failed. Given an ActionCodeSettings that does not have canHandleCodeInApp set to true")
        else -> ServiceError("Firebase: Sending reset email failed", this)
    }

    private fun Exception.toUpdateProfileError(): AuthResponseError? = when (this) {
        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AccountNotFound
        else -> ServiceError("Firebase: Profile updating failed", this)
    }

    private fun Exception.toUpdatePasswordError(): AuthResponseError? = when (this) {
        // thrown if the password is not strong enough
        is FirebaseAuthWeakPasswordException -> WeakPassword
        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AccountNotFound
        // thrown if the user's last sign-in time does not meet the security threshold.
        is FirebaseAuthRecentLoginRequiredException -> RecentLoginRequired
        else -> ServiceError("Firebase: Password updating failed", this)
    }

    private fun Exception.toUpdateEmailError(): AuthResponseError? = when (this) {
        // thrown if the email address is malformed
        is FirebaseAuthInvalidCredentialsException -> MalformedEmail
        // thrown if there already exists an account with the given email address
        is FirebaseAuthUserCollisionException -> EmailAlreadyExist
        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AccountNotFound
        // thrown if the user's last sign-in time does not meet the security threshold.
        // Use reauthenticate(AuthCredential) to resolve. This does not apply if the user is anonymous.
        is FirebaseAuthRecentLoginRequiredException -> RecentLoginRequired
        // Unhandled case
        else -> ServiceError("Firebase: Email updating failed", this)
    }

    private fun MutableLiveData<Event<AuthResponse<UserProfileDataType>>>.postResult(
        isSuccessful: Boolean,
        error: AuthResponseError?
    ) {
        postEvent(AuthResponse(
            status = if (isSuccessful) SUCCESS else FAILED,
            error = if (isSuccessful) null else error,
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