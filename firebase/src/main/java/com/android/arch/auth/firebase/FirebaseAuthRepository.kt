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
import com.google.android.gms.tasks.Task
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
            firebaseUser
                .reauthenticate(EmailAuthProvider.getCredential(email, oldPassword))
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        firebaseUser
                            .updatePassword(newPassword)
                            .addOnCompleteListener {
                                response.postResult(it, ::handleUpdatePasswordError)
                            }
                    } else {
                        response.postResult(authTask, ::handleSignInWithEmailError)
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
                response.postResult(it, ::handleSendPasswordResetEmailError)
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
                    auth.currentUser
                        ?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(login).build())
                        ?.addOnCompleteListener { task ->
                            auth.currentUser?.sendEmailVerification()
                            response.postResult(task, ::handleUpdateProfileError)
                        } ?: response.postError(AccountNotFoundAuthError)
                } else {
                    response.postResult(it, ::handleSignUpWithEmailError)
                }
            }
    }

    override fun signInWithEmail(
        email: String,
        password: String,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                response.postResult(it, ::handleSignInWithEmailError)
            }
    }

    override fun signInWithSocialNetwork(
        socialNetwork: SocialNetworkType,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ) {
        getService(socialNetwork)
            ?.signIn { response.signInWithCredential(socialNetwork, it) }
            ?: response.postError(AuthError.CanceledAuthError)
    }

    /**
     * this is a security sensitive operation that requires the user to have recently signed in.
     */
    override fun editProfile(
        request: EditProfileRequest,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ): Unit = with(request) {
        auth.currentUser?.apply {
            if (loginParam.isChanged || photoUriParam.isChanged) {
                updateProfile(UserProfileChangeRequest.Builder()
                    .apply { if (loginParam.isChanged) setDisplayName(loginParam.value) }
                    .apply { if (photoUriParam.isChanged) setPhotoUri(photoUriParam.value) }
                    .build())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            updateEmail(request, response)
                        } else {
                            response.postResult(task, ::handleUpdateProfileError)
                        }
                    }
            } else {
                updateEmail(request, response)
            }
        } ?: response.postError(AccountNotFoundAuthError)
    }

    private fun FirebaseUser.updateEmail(
        request: EditProfileRequest,
        response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>
    ): Unit = with(request) {
        if (emailParam.isChanged) {
            val email = emailParam.value
            if (email.isNullOrEmpty()) {
                response.postError(EmailRequiredAuthError)
            } else {
                updateEmail(email)
                    .addOnCompleteListener {
                        response.postResult(it, ::handleUpdateEmailError)
                    }
            }
        } else {
            response.postEvent(AuthResponse(SUCCESS, null, userProfile))
        }
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
        response.getAuthCredential(socialNetwork)?.let { credential ->
            auth.signInWithCredential(credential)
                .addOnCompleteListener {
                    postResult(it, ::handleSignInWithCredentialsError)
                }
        } ?: postError(response.error ?: CanceledAuthError)

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


    private fun handleSignInWithCredentialsError(exception: Exception): AuthError = when (exception) {
        // thrown if the user account you are trying to sign in to has been disabled.
        // Also thrown if credential is an EmailAuthCredential with an email address that does not correspond to an existing user.
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError
        // thrown if the credential is malformed or has expired.
        // If credential instanceof EmailAuthCredential it will be thrown if the password is incorrect.
        is FirebaseAuthInvalidCredentialsException -> InvalidCredentialsAuthError
        // thrown if there already exists an account with the email address asserted by the credential.
        // Resolve this case by calling fetchProvidersForEmail(String) and then asking the user to sign in using one of them.
        is FirebaseAuthUserCollisionException -> AccountsCollisionAuthError
        else -> ServiceAuthError("Firebase: Sign in with credentials failed", exception)
    }

    private fun handleSignInWithEmailError(exception: Exception): AuthError = when (exception) {
        // thrown if the user account corresponding to email does not exist or has been disabled
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError
        // thrown if the password is wrong
        is FirebaseAuthInvalidCredentialsException -> AuthError.WrongPasswordAuthError
        else -> ServiceAuthError("Firebase: Sign in with email failed", exception)
    }

    private fun handleSignUpWithEmailError(exception: Exception): AuthError = when (exception) {
        // thrown if the password is not strong enough
        is FirebaseAuthWeakPasswordException -> WeakPasswordAuthError
        // thrown if the email address is malformed
        is FirebaseAuthInvalidCredentialsException -> MalformedEmailAuthError
        // thrown if there already exists an account with the given email address
        is FirebaseAuthUserCollisionException -> EmailAlreadyExistAuthError
        else -> ServiceAuthError("Firebase: Sign up with email failed", exception)
    }

    private fun handleSendPasswordResetEmailError(exception: Exception): AuthError = when (exception) {
        // thrown if there is no user corresponding to the given email address
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError
        else -> ServiceAuthError("Firebase: Sending reset email failed", exception)
    }

    private fun handleUpdateProfileError(exception: Exception): AuthError = when (exception) {
        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError
        else -> ServiceAuthError("Firebase: Profile updating failed", exception)
    }

    private fun handleUpdatePasswordError(exception: Exception): AuthError = when (exception) {
        // thrown if the password is not strong enough
        is FirebaseAuthWeakPasswordException -> WeakPasswordAuthError
        // thrown if the current user's account has been disabled, deleted, or its credentials are no longer valid
        is FirebaseAuthInvalidUserException -> AccountNotFoundAuthError
        // thrown if the user's last sign-in time does not meet the security threshold.
        is FirebaseAuthRecentLoginRequiredException -> RecentLoginRequiredAuthError
        else -> ServiceAuthError("Firebase: Password updating failed", exception)
    }

    private fun handleUpdateEmailError(exception: Exception): AuthError = when (exception) {
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
        else -> ServiceAuthError("Firebase: Email updating failed", exception)
    }

    private fun MutableLiveData<Event<AuthResponse<UserProfileDataType>>>.postResult(
        task: Task<*>,
        handleError: (Exception) -> AuthError
    ) {
        postEvent(
            AuthResponse(
                status = if (task.isSuccessful) SUCCESS else FAILED,
                error = if (task.isSuccessful) null else task.exception?.let { handleError(it) },
                data = userProfile
            )
        )
    }

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    private val userProfile: UserProfileDataType?
        get() = auth.currentUser?.let { userDataFactory.create(it) }

}