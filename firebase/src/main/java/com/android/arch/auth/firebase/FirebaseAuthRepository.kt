package com.android.arch.auth.firebase

import android.util.Log
import com.android.arch.auth.core.data.entity.*
import com.android.arch.auth.core.data.entity.AuthError.*
import com.android.arch.auth.core.data.entity.AuthRequestStatus.FAILED
import com.android.arch.auth.core.data.entity.AuthRequestStatus.SUCCESS
import com.android.arch.auth.core.data.entity.SocialNetworkType.*
import com.android.arch.auth.core.data.repository.EmailAuthRepository
import com.android.arch.auth.core.data.repository.NetworkAuthRepository
import com.android.arch.auth.core.data.repository.SocialNetworkAuthRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*

class FirebaseAuthRepository<UserProfileDataType>(
    private val userDataFactory: Factory<UserProfileDataType>
) : EmailAuthRepository<UserProfileDataType>,
    SocialNetworkAuthRepository<UserProfileDataType>,
    NetworkAuthRepository<UserProfileDataType>() {

    interface Factory<UserProfileDataType> {
        fun create(user: FirebaseUser): UserProfileDataType
    }

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    private val userProfile: UserProfileDataType?
        get() = auth.currentUser?.let { userDataFactory.create(it) }

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
            ?: postAuthError(CanceledAuthError())
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

    private fun handleSignInWithCredentialsError(exception: Exception): AuthError =
        when (exception) {
            // thrown if the user account you are trying to sign in to has been disabled.
            // Also thrown if credential is an EmailAuthCredential with an email address that does not correspond to an existing user.
            is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError(exception.message)
            // thrown if the credential is malformed or has expired.
            // If credential instanceof EmailAuthCredential it will be thrown if the password is incorrect.
            is FirebaseAuthInvalidCredentialsException -> InvalidCredentialsAuthError(exception.message)
            // thrown if there already exists an account with the email address asserted by the credential.
            // Resolve this case by calling fetchProvidersForEmail(String) and then asking the user to sign in using one of them.
            is FirebaseAuthUserCollisionException -> AccountsCollisionAuthError(exception.message)
            else -> ServiceAuthError("Firebase: Sign in with credentials failed", exception)
        }

    private fun handleSignInWithEmailError(exception: Exception): AuthError = when (exception) {
        is FirebaseTooManyRequestsException -> TooManyRequestsAuthError(exception.message)
        // thrown if the user account corresponding to email does not exist or has been disabled
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError(exception.message)
        // thrown if the password is wrong
        is FirebaseAuthInvalidCredentialsException -> WrongPasswordAuthError(exception.message)
        else -> ServiceAuthError("Firebase: Sign in with email failed", exception)
    }

    private fun handleSignUpWithEmailError(exception: Exception): AuthError = when (exception) {
        // thrown if the password is not strong enough
        is FirebaseAuthWeakPasswordException -> WeakPasswordAuthError(exception.message)
        // thrown if the email address is malformed
        is FirebaseAuthInvalidCredentialsException -> MalformedEmailAuthError(exception.message)
        // thrown if there already exists an account with the given email address
        is FirebaseAuthUserCollisionException -> EmailAlreadyExistAuthError(exception.message)
        else -> ServiceAuthError("Firebase: Sign up with email failed", exception)
    }

    private fun handleSendPasswordResetEmailError(exception: Exception): AuthError =
        when (exception) {
            // thrown if there is no user corresponding to the given email address
            is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError(exception.message)
            else -> ServiceAuthError("Firebase: Sending reset email failed", exception)
        }

    private fun handleUpdateProfileError(exception: Exception): AuthError = when (exception) {
        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError(exception.message)
        else -> ServiceAuthError("Firebase: Profile updating failed", exception)
    }

    private fun handleUpdatePasswordError(exception: Exception): AuthError = when (exception) {
        // thrown if the password is not strong enough
        is FirebaseAuthWeakPasswordException -> WeakPasswordAuthError(exception.message)
        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError(exception.message)
        // thrown if the user's last sign-in time does not meet the security threshold.
        is FirebaseAuthRecentLoginRequiredException -> RecentLoginRequiredAuthError(exception.message)
        else -> ServiceAuthError("Firebase: Password updating failed", exception)
    }

    private fun handleUpdateEmailError(exception: Exception): AuthError = when (exception) {
        // thrown if the email address is malformed
        is FirebaseAuthInvalidCredentialsException -> MalformedEmailAuthError(exception.message)
        // thrown if there already exists an account with the given email address
        is FirebaseAuthUserCollisionException -> EmailAlreadyExistAuthError(exception.message)
        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError(exception.message)
        // thrown if the user's last sign-in time does not meet the security threshold.
        // Use reauthenticate(AuthCredential) to resolve. This does not apply if the user is anonymous.
        is FirebaseAuthRecentLoginRequiredException -> RecentLoginRequiredAuthError(exception.message)
        // Unhandled case
        else -> ServiceAuthError("Firebase: Email updating failed", exception)
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