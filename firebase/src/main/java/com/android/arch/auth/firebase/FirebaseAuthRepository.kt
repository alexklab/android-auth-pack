package com.android.arch.auth.firebase

import android.util.Log
import com.android.arch.auth.core.data.entity.*
import com.android.arch.auth.core.data.entity.AuthError.*
import com.android.arch.auth.core.data.entity.AuthRequestStatus.FAILED
import com.android.arch.auth.core.data.entity.AuthRequestStatus.SUCCESS
import com.android.arch.auth.core.data.entity.SocialNetworkType.*
import com.android.arch.auth.core.data.network.NetworkSignInService
import com.android.arch.auth.core.data.repository.EmailAuthRepository
import com.android.arch.auth.core.data.repository.NetworkAuthRepository
import com.android.arch.auth.core.data.repository.SocialNetworkAuthRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*

class FirebaseAuthRepository<UserProfileDataType>(
    private val transform: (FirebaseUser) -> UserProfileDataType,
    vararg services: NetworkSignInService
) : EmailAuthRepository<UserProfileDataType>,
    SocialNetworkAuthRepository<UserProfileDataType>,
    NetworkAuthRepository<UserProfileDataType>(*services) {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    private val userProfile: UserProfileDataType? get() = auth.currentUser?.let { transform(it) }

    override fun signOut() {
        auth.signOut()
        allServicesSignOut()
    }

    /**
     * this is a security sensitive operation that requires the user to have recently signed in.
     */
    override fun changePassword(uid: String, oldPassword: String, newPassword: String) {
        val firebaseUser = auth.currentUser
        val email = firebaseUser?.providerData
            ?.first { it.providerId == EmailAuthProvider.PROVIDER_ID }
            ?.email
        if (email != null) {
            firebaseUser
                .reauthenticate(EmailAuthProvider.getCredential(email, oldPassword))
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        firebaseUser
                            .updatePassword(newPassword)
                            .addOnCompleteListener {
                                postAuthResponse(it, ::handleUpdatePasswordError)
                            }
                    } else {
                        postAuthResponse(authTask, ::handleSignInWithEmailError)
                    }
                }
        } else {
            postAuthError(AccountNotFoundAuthError())
        }
    }

    override fun recoverPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                postAuthResponse(it, ::handleSendPasswordResetEmailError)
            }
    }

    override fun signUp(login: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    auth.currentUser
                        ?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(login).build())
                        ?.addOnCompleteListener { task ->
                            auth.currentUser?.sendEmailVerification()
                            postAuthResponse(task, ::handleUpdateProfileError)
                        } ?: postAuthError(AccountNotFoundAuthError())
                } else {
                    postAuthResponse(it, ::handleSignUpWithEmailError)
                }
            }
    }

    override fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                postAuthResponse(it, ::handleSignInWithEmailError)
            }
    }

    override fun signInWithSocialNetwork(socialNetwork: SocialNetworkType) {
        getService(socialNetwork)
            ?.signIn()
            ?: postAuthError(ServiceAuthError("NetworkService[$socialNetwork] not found"))
    }

    /**
     * this is a security sensitive operation that requires the user to have recently signed in.
     */
    override fun editProfile(request: EditProfileRequest): Unit = with(request) {
        auth.currentUser?.apply {
            if (loginParam.isChanged || photoUriParam.isChanged) {
                updateProfile(UserProfileChangeRequest.Builder()
                    .apply { if (loginParam.isChanged) setDisplayName(loginParam.value) }
                    .apply { if (photoUriParam.isChanged) setPhotoUri(photoUriParam.value) }
                    .build())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            updateEmail(request)
                        } else {
                            postAuthResponse(task, ::handleUpdateProfileError)
                        }
                    }
            } else {
                updateEmail(request)
            }
        } ?: postAuthError(AccountNotFoundAuthError())
    }

    override fun onSignInResponse(socialNetwork: SocialNetworkType, response: SignInResponse) {
        val authCredential = response.getAuthCredential(socialNetwork)
        if (authCredential != null) {
            auth.signInWithCredential(authCredential)
                .addOnCompleteListener {
                    postAuthResponse(it, ::handleSignInWithCredentialsError)
                }
        } else {
            postAuthError(response.error ?: CanceledAuthError())
        }
    }

    override fun sendVerifiedEmailKeyUseCase(verifyKey: String) {
        // implemented on firebase back side
        postAuthError(ServiceAuthError("Unsupported method usage. Actually implemented on Firebase back side"))
    }

    private fun FirebaseUser.updateEmail(request: EditProfileRequest): Unit = with(request) {
        if (emailParam.isChanged) {
            val email = emailParam.value
            if (email.isNullOrEmpty()) {
                postAuthError(EmailRequiredAuthError())
            } else {
                updateEmail(email).addOnCompleteListener {
                    postAuthResponse(it, ::handleUpdateEmailError)
                }
            }
        } else {
            postAuthResponse(AuthResponse(SUCCESS, null, userProfile))
        }
    }

    private fun SignInResponse.getAuthCredential(socialNetwork: SocialNetworkType): AuthCredential? {
        val responseToken = token
        if (responseToken == null) {
            Log.e("getAuthCredential", "Undefined token. Provider $socialNetwork")
            return null
        }
        return when (socialNetwork) {
            GOOGLE -> GoogleAuthProvider.getCredential(responseToken, null)
            FACEBOOK -> FacebookAuthProvider.getCredential(responseToken)
            TWITTER -> {
                val responseTokenSecret = tokenSecret
                if (responseTokenSecret == null) {
                    Log.e("getAuthCredential", "Undefined tokenSecret. Provider $socialNetwork")
                    null
                } else {
                    TwitterAuthProvider.getCredential(responseToken, responseTokenSecret)
                }
            }
            else -> null
        }
    }

    private fun handleSignInWithCredentialsError(e: Exception): AuthError = when (e) {
        // thrown if the user account you are trying to sign in to has been disabled.
        // Also thrown if credential is an EmailAuthCredential with an email address that does not correspond to an existing user.
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError(e)
        // thrown if the credential is malformed or has expired.
        // If credential instanceof EmailAuthCredential it will be thrown if the password is incorrect.
        is FirebaseAuthInvalidCredentialsException -> InvalidCredentialsAuthError(e)
        // thrown if there already exists an account with the email address asserted by the credential.
        // Resolve this case by calling fetchProvidersForEmail(String) and then asking the user to sign in using one of them.
        is FirebaseAuthUserCollisionException -> AccountsCollisionAuthError(e)
        else -> ServiceAuthError("Firebase: Sign in with credentials failed", e)
    }

    private fun handleSignInWithEmailError(e: Exception): AuthError = when (e) {
        is FirebaseTooManyRequestsException -> TooManyRequestsAuthError(e)
        // thrown if the user account corresponding to email does not exist or has been disabled
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError(e)
        // thrown if the password is wrong
        is FirebaseAuthInvalidCredentialsException -> WrongPasswordAuthError(e)
        else -> ServiceAuthError("Firebase: Sign in with email failed", e)
    }

    private fun handleSignUpWithEmailError(e: Exception): AuthError = when (e) {
        // thrown if the password is not strong enough
        is FirebaseAuthWeakPasswordException -> WeakPasswordAuthError(e)
        // thrown if the email address is malformed
        is FirebaseAuthInvalidCredentialsException -> MalformedEmailAuthError(e)
        // thrown if there already exists an account with the given email address
        is FirebaseAuthUserCollisionException -> EmailAlreadyExistAuthError(e)
        else -> ServiceAuthError("Firebase: Sign up with email failed", e)
    }

    private fun handleSendPasswordResetEmailError(e: Exception): AuthError = when (e) {
        // thrown if there is no user corresponding to the given email address
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError(e)
        else -> ServiceAuthError("Firebase: Sending reset email failed", e)
    }

    private fun handleUpdateProfileError(e: Exception): AuthError = when (e) {
        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError(e)
        else -> ServiceAuthError("Firebase: Profile updating failed", e)
    }

    private fun handleUpdatePasswordError(e: Exception): AuthError = when (e) {
        // thrown if the password is not strong enough
        is FirebaseAuthWeakPasswordException -> WeakPasswordAuthError(e)
        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError(e)
        // thrown if the user's last sign-in time does not meet the security threshold.
        is FirebaseAuthRecentLoginRequiredException -> RecentLoginRequiredAuthError(e)
        else -> ServiceAuthError("Firebase: Password updating failed", e)
    }

    private fun handleUpdateEmailError(e: Exception): AuthError = when (e) {
        // thrown if the email address is malformed
        is FirebaseAuthInvalidCredentialsException -> MalformedEmailAuthError(e)
        // thrown if there already exists an account with the given email address
        is FirebaseAuthUserCollisionException -> EmailAlreadyExistAuthError(e)
        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError(e)
        // thrown if the user's last sign-in time does not meet the security threshold.
        // Use reauthenticate(AuthCredential) to resolve. This does not apply if the user is anonymous.
        is FirebaseAuthRecentLoginRequiredException -> RecentLoginRequiredAuthError(e)
        // Unhandled case
        else -> ServiceAuthError("Firebase: Email updating failed", e)
    }

    private fun postAuthResponse(task: Task<*>, handleError: (Exception) -> AuthError) =
        postAuthResponse(
            AuthResponse(
                status = if (task.isSuccessful) SUCCESS else FAILED,
                error = if (task.isSuccessful) null else task.exception?.let { handleError(it) },
                data = userProfile
            )
        )
}