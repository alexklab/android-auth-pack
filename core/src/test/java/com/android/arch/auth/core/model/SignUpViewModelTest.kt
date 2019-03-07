package com.android.arch.auth.core.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.common.FieldValidator
import com.android.arch.auth.core.common.extensions.postEvent
import com.android.arch.auth.core.domain.auth.SignUpUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase
import com.android.arch.auth.core.data.entity.AuthRequestStatus.FAILED
import com.android.arch.auth.core.data.entity.AuthRequestStatus.SUCCESS
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.AuthResponseError
import com.android.arch.auth.core.data.entity.AuthResponseError.*
import com.android.arch.auth.core.data.entity.Event
import com.android.arch.auth.core.data.repository.EmailAuthRepository
import com.android.arch.auth.core.data.repository.UserProfileDataCache
import com.android.arch.auth.core.testutils.CoroutineContextProviderRule
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.amshove.kluent.any
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class SignUpViewModelTest : AuthBaseViewModelTest<UserProfile, SignUpViewModel<UserProfile>>() {

    override val instance: SignUpViewModel<UserProfile>
        get() = SignUpViewModel(
                emailValidator,
                loginValidator,
                passwordValidator,
                SignUpUseCase(repository),
                UpdateProfileUseCase(cache)
        ).apply { authResponse = getRawResponseData() }

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a the same thread
    @get:Rule
    var coroutineContextProviderRule = CoroutineContextProviderRule()

    @Mock
    private lateinit var repository: EmailAuthRepository<UserProfile>
    @Mock
    private lateinit var cache: UserProfileDataCache<UserProfile>
    @Mock
    private lateinit var emailValidator: FieldValidator
    @Mock
    private lateinit var loginValidator: FieldValidator
    @Mock
    private lateinit var passwordValidator: FieldValidator

    private var signUpResponseError: AuthResponseError? = null
    private val customError = AuthResponseError.ServiceError("Custom Error")

    private lateinit var authResponse: MutableLiveData<Event<AuthResponse<UserProfile>>>

    private companion object {

        const val EMPTY = ""

        const val VALID_LOGIN = "User"
        const val VALID_EMAIL = "test@gmail.com"
        const val VALID_PASSWORD = "test1234"

        const val invalidLogin = "t"
        const val invalidEmail = "test@com"
        const val invalidPassword = "test"

        private val profile = UserProfile("user_unique_id_123", VALID_LOGIN, VALID_EMAIL)
    }

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        signUpResponseError = null
        `when`(emailValidator.validate(any())).thenAnswer { it.arguments[0] == VALID_EMAIL }
        `when`(loginValidator.validate(any())).thenAnswer { it.arguments[0] == VALID_LOGIN }
        `when`(passwordValidator.validate(any())).thenAnswer { it.arguments[0] == VALID_PASSWORD }
        `when`(repository.signUp(any(), any(), any(), any())).thenAnswer {
            (it.arguments.last() as MutableLiveData<Event<AuthResponse<UserProfile>>>)
                    .postEvent(signUpResponseError.toAuthResponse(profile))
        }
    }

    @Test
    fun `signUp() login should be NOT empty`() = responseTestCase(
            action = {
                signUp(login = EMPTY,
                        email = VALID_EMAIL,
                        password = VALID_PASSWORD,
                        confirmPassword = VALID_PASSWORD,
                        isEnabledTermsOfUse = true)
            },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(AuthResponseError.LoginRequired, result?.error)
                verifyZeroInteractions(repository, cache)
            }
    )

    @Test
    fun `signUp() email should be NOT empty`() = responseTestCase(
            action = {
                signUp(login = VALID_LOGIN,
                        email = EMPTY,
                        password = VALID_PASSWORD,
                        confirmPassword = VALID_PASSWORD,
                        isEnabledTermsOfUse = true)
            },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(AuthResponseError.EmailRequired, result?.error)
                verifyZeroInteractions(repository, cache)
            }
    )

    @Test
    fun `signUp() password should be NOT empty`() = responseTestCase(
            action = {
                signUp(login = VALID_LOGIN,
                        email = VALID_EMAIL,
                        password = EMPTY,
                        confirmPassword = VALID_PASSWORD,
                        isEnabledTermsOfUse = true)
            },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(PasswordRequired, result?.error)
                verifyZeroInteractions(repository, cache)
            }
    )

    @Test
    fun `signUp() confirm password should be NOT empty`() = responseTestCase(
            action = {
                signUp(login = VALID_LOGIN,
                        email = VALID_EMAIL,
                        password = VALID_PASSWORD,
                        confirmPassword = EMPTY,
                        isEnabledTermsOfUse = true)
            },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(ConfirmPasswordRequired, result?.error)
                verifyZeroInteractions(repository, cache)
            }
    )

    @Test
    fun `signUp() term of use should be ENABLED`() = responseTestCase(
            action = {
                signUp(login = VALID_LOGIN,
                        email = VALID_EMAIL,
                        password = VALID_PASSWORD,
                        confirmPassword = VALID_PASSWORD,
                        isEnabledTermsOfUse = false)
            },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(EnableTermsOfUseRequired, result?.error)
                verifyZeroInteractions(repository, cache)
            }
    )

    @Test
    fun `updateProfile login should be valid`() = responseTestCase(
            action = {
                signUp(login = invalidLogin,
                        email = VALID_EMAIL,
                        password = VALID_PASSWORD,
                        confirmPassword = VALID_PASSWORD,
                        isEnabledTermsOfUse = true)
            },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(MalformedLogin, result?.error)
                verifyZeroInteractions(repository, cache)
            }
    )

    @Test
    fun `signUp() password should be valid`() = responseTestCase(
            action = {
                signUp(login = VALID_LOGIN,
                        email = VALID_EMAIL,
                        password = invalidPassword,
                        confirmPassword = VALID_PASSWORD,
                        isEnabledTermsOfUse = true)
            },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(WeakPassword, result?.error)
                verifyZeroInteractions(repository, cache)
            }
    )

    @Test
    fun `signUp() email should be valid`() = responseTestCase(
            action = {
                signUp(login = VALID_LOGIN,
                        email = invalidEmail,
                        password = VALID_PASSWORD,
                        confirmPassword = VALID_PASSWORD,
                        isEnabledTermsOfUse = true)
            },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(MalformedEmail, result?.error)
                verifyZeroInteractions(repository, cache)
            }
    )

    @Test
    fun `signUp() should handle success on service response SUCCESS`() = responseTestCase(
            setup = { signUpResponseError = null /* success */ },
            action = { signUp(VALID_LOGIN, VALID_EMAIL, VALID_PASSWORD, VALID_PASSWORD, true) },
            expected = {
                assertEquals(SUCCESS, it?.status)
                verify(repository).signUp(VALID_LOGIN, VALID_EMAIL, VALID_PASSWORD, authResponse)
                verify(cache).updateProfile(profile)
                verifyNoMoreInteractions(repository, cache)
            }
    )

    @Test
    fun `signUp() should handle error on service response AUTH_SERVICE_ERROR`() = responseTestCase(
            setup = { signUpResponseError = customError },
            action = { signUp(VALID_LOGIN, VALID_EMAIL, VALID_PASSWORD, VALID_PASSWORD, true) },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(customError, result?.error)
                verify(repository).signUp(VALID_LOGIN, VALID_EMAIL, VALID_PASSWORD, authResponse)
                verifyNoMoreInteractions(repository)
                verifyZeroInteractions(cache)
            }
    )

    @Test
    fun `signUp() should handle error on service response ACCOUNT_ALREADY_EXIST`() = responseTestCase(
            setup = { signUpResponseError = EmailAlreadyExist },
            action = { signUp(VALID_LOGIN, VALID_EMAIL, VALID_PASSWORD, VALID_PASSWORD, true) },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(EmailAlreadyExist, result?.error)
                verify(repository).signUp(VALID_LOGIN, VALID_EMAIL, VALID_PASSWORD, authResponse)
                verifyNoMoreInteractions(repository)
                verifyZeroInteractions(cache)
            }
    )

    @Test
    fun `signUp() should handle error on service response AUTH_LOGIN_ALREADY_EXIST`() = responseTestCase(
            setup = { signUpResponseError = LoginAlreadyExist },
            action = { signUp(VALID_LOGIN, VALID_EMAIL, VALID_PASSWORD, VALID_PASSWORD, true) },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(LoginAlreadyExist, result?.error)
                verify(repository).signUp(VALID_LOGIN, VALID_EMAIL, VALID_PASSWORD, authResponse)
                verifyNoMoreInteractions(repository)
                verifyZeroInteractions(cache)
            }
    )
}