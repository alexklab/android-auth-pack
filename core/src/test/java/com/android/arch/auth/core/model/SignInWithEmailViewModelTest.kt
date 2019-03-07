package com.android.arch.auth.core.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.common.extensions.postEvent
import com.android.arch.auth.core.domain.auth.SignInWithEmailUseCase
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
import org.amshove.kluent.any
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.util.*

/**
 * Created by alexk on 11/26/18.
 * Project android-auth-pack
 */
class SignInWithEmailViewModelTest : AuthBaseViewModelTest<UserProfile, SignInWithEmailViewModel<UserProfile>>() {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a the same thread
    @get:Rule
    var coroutineContextProviderRule = CoroutineContextProviderRule()

    override val instance: SignInWithEmailViewModel<UserProfile>
        get() = SignInWithEmailViewModel(
                SignInWithEmailUseCase(repository),
                UpdateProfileUseCase(cache)).apply { authResponse = getRawResponseData() }

    @Mock
    private lateinit var repository: EmailAuthRepository<UserProfile>
    @Mock
    private lateinit var cache: UserProfileDataCache<UserProfile>

    private var signInWithEmailResponse: AuthResponseError? = null
    private val customError = ServiceError("Custom Error")
    private lateinit var authResponse: MutableLiveData<Event<AuthResponse<UserProfile>>>

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(repository.signInWithEmail(any(), any(), any())).thenAnswer {
            (it.arguments.last() as MutableLiveData<Event<AuthResponse<UserProfile>>>)
                    .postEvent(signInWithEmailResponse.toAuthResponse(profile))
        }
    }

    @Test
    fun `signInWithEmail() email should be NOT empty`() = responseTestCase(
            action = { signInWithEmail(email = EMPTY_VALUE, password = VALID_PASSWORD) },
            expected = { response ->
                assertEquals(FAILED, response?.status)
                assertEquals(EmailRequired, response?.error)
                verifyZeroInteractions(repository, cache)
            })

    @Test
    fun `signInWithEmail() password should be NOT empty`() = responseTestCase(
            action = { signInWithEmail(email = VALID_EMAIL, password = EMPTY_VALUE) },
            expected = { response ->
                assertEquals(FAILED, response?.status)
                assertEquals(PasswordRequired, response?.error)
                verifyZeroInteractions(repository, cache)
            })

    @Test
    fun `signInWithEmail() should handle error on service response AUTH_SERVICE_ERROR`() = responseTestCase(
            setup = { signInWithEmailResponse = customError },
            action = { signInWithEmail(VALID_EMAIL, VALID_PASSWORD) },
            expected = { response ->
                assertEquals(FAILED, response?.status)
                assertEquals(customError, response?.error)
                verify(repository).signInWithEmail(VALID_EMAIL, VALID_PASSWORD, authResponse)
                verifyNoMoreInteractions(repository)
                verifyZeroInteractions(cache)
            })

    @Test
    fun `signInWithEmail() should handle error on service response AUTH_WRONG_PASSWORD`() = responseTestCase(
            setup = { signInWithEmailResponse = WrongPassword },
            action = { signInWithEmail(VALID_EMAIL, VALID_PASSWORD) },
            expected = { response ->
                assertEquals(FAILED, response?.status)
                assertEquals(WrongPassword, response?.error)
                verify(repository).signInWithEmail(VALID_EMAIL, VALID_PASSWORD, authResponse)
                verifyNoMoreInteractions(repository)
                verifyZeroInteractions(cache)
            })

    @Test
    fun `signInWithEmail() should handle error on service response AUTH_ACCOUNT_NOT_FOUND`() = responseTestCase(
            setup = { signInWithEmailResponse = AccountNotFound },
            action = { signInWithEmail(VALID_EMAIL, VALID_PASSWORD) },
            expected = { response ->
                assertEquals(FAILED, response?.status)
                assertEquals(AccountNotFound, response?.error)
                verify(repository).signInWithEmail(VALID_EMAIL, VALID_PASSWORD, authResponse)
                verifyNoMoreInteractions(repository)
                verifyZeroInteractions(cache)
            })

    @Test
    fun `signInWithEmail() should handle error on service response AUTH_ACCOUNT_NOT_ACTIVATED`() = responseTestCase(
            setup = { signInWithEmailResponse = AccountNotActivated },
            action = { signInWithEmail(VALID_EMAIL, VALID_PASSWORD) },
            expected = { response ->
                assertEquals(FAILED, response?.status)
                assertEquals(AccountNotActivated, response?.error)
                verify(repository).signInWithEmail(VALID_EMAIL, VALID_PASSWORD, authResponse)
                verifyNoMoreInteractions(repository)
                verifyZeroInteractions(cache)
            })

    @Test
    fun `signInWithEmail() should handle success on service response SUCCESS`() = responseTestCase(
            setup = { signInWithEmailResponse = null /* success */ },
            action = { signInWithEmail(VALID_EMAIL, VALID_PASSWORD) },
            expected = { response ->
                assertEquals(SUCCESS, response?.status)
                verify(repository).signInWithEmail(VALID_EMAIL, VALID_PASSWORD, authResponse)
                verify(cache).updateProfile(profile)
                verifyNoMoreInteractions(repository, cache)
            })

    private companion object {
        private const val EMPTY_VALUE = ""
        private const val VALID_EMAIL = "as@as.co"
        private const val VALID_PASSWORD = "1234qwe"

        private val profile = UserProfile(
                login = "user_name",
                email = "user@mail.com",
                uid = UUID.randomUUID().toString()
        )

    }
}