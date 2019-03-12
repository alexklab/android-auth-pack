package com.android.arch.auth.firebase

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.common.extensions.postError
import com.android.arch.auth.core.common.extensions.postEvent
import com.android.arch.auth.core.data.entity.*
import com.android.arch.auth.core.data.entity.AuthError.*
import com.android.arch.auth.core.data.entity.AuthRequestStatus.FAILED
import com.android.arch.auth.core.data.entity.AuthRequestStatus.SUCCESS
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
        val firebaseUser = auth.currentUser
        val email = firebaseUser?.providerData
            ?.first { it.providerId == EmailAuthProvider.PROVIDER_ID }
            ?.email
        if (email != null) {
            firebaseUser.reauthenticate(EmailAuthProvider.getCredential(email, oldPassword))
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        firebaseUser.updatePassword(newPassword).addOnCompleteListener {
                            response.postResult(it.isSuccessful, it.exception?.toUpdatePasswordError())
                        }
                    } else {
                        response.postResult(authTask.isSuccessful, authTask.exception?.toSignInWithEmailError())
                    }
                }
        } else {
            response.postError(AccountNotFoundAuthError)
        }
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
                val user = auth.currentUser
                if (it.isSuccessful && user != null) {
                    user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(login).build())
                        .addOnCompleteListener { task ->
                            user.sendEmailVerification()
                            response.postResult(task.isSuccessful, task.exception?.toUpdateProfileError())
                        }
                } else {
                    response.postResult(it.isSuccessful, it.exception?.toSignUpWithEmailError())
                }
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
        } ?: response.postError(AuthError.CanceledAuthError)
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
                .setPhotoUri(photoUrl)
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
        } ?: response.postError(AccountNotFoundAuthError)
    }

    override fun sendVerifiedEmailKeyUseCase(
        verifyKey: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        // implemented on firebase back side
        response.postError(ServiceAuthError("Unsupported method usage. Actually implemented on Firebase back side"))
    }

    private fun MutableLiveData<Event<AuthResponse<UserProfileDataType>>>.signInWithCredential(
        socialNetwork: SocialNetworkType,
        response: SignInResponse
    ) {
        getAuthCredential(socialNetwork, response)?.let { credential ->
            auth.signInWithCredential(credential)
                .addOnCompleteListener { postResult(it.isSuccessful, it.exception?.toSignInWithCredentialsError()) }
        } ?: postError(response.error ?: CanceledAuthError)

    }

    private fun Exception.toSignInWithCredentialsError(): AuthError? = when (this) {
        // thrown if the user account you are trying to sign in to has been disabled.
        // Also thrown if credential is an EmailAuthCredential with an email address that does not correspond to an existing user.
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError
        // thrown if the credential is malformed or has expired.
        // If credential instanceof EmailAuthCredential it will be thrown if the password is incorrect.
        is FirebaseAuthInvalidCredentialsException -> InvalidCredentialsAuthError
        // thrown if there already exists an account with the email address asserted by the credential.
        // Resolve this case by calling fetchProvidersForEmail(String) and then asking the user to sign in using one of them.
        is FirebaseAuthUserCollisionException -> AccountsCollisionAuthError
        else -> ServiceAuthError("Firebase: Sign in with credentials failed", this)
    }


    private fun Exception.toSignInWithEmailError(): AuthError? = when (this) {
        // thrown if the user account corresponding to email does not exist or has been disabled
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError
        // thrown if the password is wrong
        is FirebaseAuthInvalidCredentialsException -> AuthError.WrongPasswordAuthError
        else -> ServiceAuthError("Firebase: Sign in with email failed", this)
    }

    private fun Exception.toSignUpWithEmailError(): AuthError? = when (this) {
        // thrown if the password is not strong enough
        is FirebaseAuthWeakPasswordException -> WeakPasswordAuthError
        // thrown if the email address is malformed
        is FirebaseAuthInvalidCredentialsException -> MalformedEmailAuthError
        // thrown if there already exists an account with the given email address
        is FirebaseAuthUserCollisionException -> EmailAlreadyExistAuthError
        else -> ServiceAuthError("Firebase: Sign up with email failed", this)
    }

    private fun Exception.toSendPasswordResetEmailError(): AuthError? = when (this) {
        // thrown if there is no user corresponding to the given email address
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError
        else -> ServiceAuthError("Firebase: Sending reset email failed", this)
    }

    private fun Exception.toUpdateProfileError(): AuthError? = when (this) {
        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError
        else -> ServiceAuthError("Firebase: Profile updating failed", this)
    }

    private fun Exception.toUpdatePasswordError(): AuthError? = when (this) {
        // thrown if the password is not strong enough
        is FirebaseAuthWeakPasswordException -> WeakPasswordAuthError
        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError
        // thrown if the user's last sign-in time does not meet the security threshold.
        is FirebaseAuthRecentLoginRequiredException -> RecentLoginRequiredAuthError
        else -> ServiceAuthError("Firebase: Password updating failed", this)
    }

    private fun Exception.toUpdateEmailError(): AuthError? = when (this) {
        // thrown if the email address is malformed
        is FirebaseAuthInvalidCredentialsException -> MalformedEmailAuthError
        // thrown if there already exists an account with the given email address
        is FirebaseAuthUserCollisionException -> EmailAlreadyExistAuthError
        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError
        // thrown if the user's last sign-in time does not meet the security threshold.
        // Use reauthenticate(AuthCredential) to resolve. This does not apply if the user is anonymous.
        is FirebaseAuthRecentLoginRequiredException -> RecentLoginRequiredAuthError
        // Unhandled case
        else -> ServiceAuthError("Firebase: Email updating failed", this)
    }

    private fun MutableLiveData<Event<AuthResponse<UserProfileDataType>>>.postResult(
        isSuccessful: Boolean,
        error: AuthError?
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