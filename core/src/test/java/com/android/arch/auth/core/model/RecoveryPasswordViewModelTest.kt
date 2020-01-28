package com.android.arch.auth.core.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.common.FieldValidator
import com.android.arch.auth.core.common.extensions.postEvent
import com.android.arch.auth.core.data.entity.AuthError
import com.android.arch.auth.core.data.entity.AuthError.*
import com.android.arch.auth.core.data.entity.AuthRequestStatus.FAILED
import com.android.arch.auth.core.data.entity.AuthRequestStatus.SUCCESS
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.Event
import com.android.arch.auth.core.data.repository.EmailAuthRepository
import com.android.arch.auth.core.domain.auth.RecoveryPasswordUseCase
import com.android.arch.auth.core.testutils.CoroutineContextProviderRule
import org.amshove.kluent.any
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class RecoveryPasswordViewModelTest : AuthBaseViewModelTest<UserProfile, RecoveryPasswordViewModel<UserProfile>>() {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a the same thread
    @get:Rule
    var coroutineContextProviderRule = CoroutineContextProviderRule()

    @Mock
    private lateinit var repository: EmailAuthRepository<UserProfile>
    @Mock
    private lateinit var emailValidator: FieldValidator

    private lateinit var authResponse: MutableLiveData<Event<AuthResponse<UserProfile>>>

    private var recoverPasswordError: AuthError? = null
    private val customError = ServiceAuthError("Custom Error")

    override val instance: RecoveryPasswordViewModel<UserProfile>
        get() = RecoveryPasswordViewModel(
                emailValidator,
                RecoveryPasswordUseCase(repository)
        ).apply { authResponse = getRawResponseData() }

    private val emptyEmail = ""
    private val invalidEmail = "asdadad@asdada"
    private val validEmail = "test@gmail.com"

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        recoverPasswordError = null
        `when`(emailValidator.validate(any())).thenAnswer { it.arguments[0] == validEmail }
        `when`(repository.recoverPassword(any(), any())).thenAnswer {
            (it.arguments.last() as MutableLiveData<Event<AuthResponse<UserProfile>>>)
                    .postEvent(recoverPasswordError.toAuthResponse())
        }
    }

    @Test
    fun `sendRecoveryPasswordRequest() email should be NOT empty`() = responseTestCase(
            action = { sendRecoveryPasswordRequest(emptyEmail) },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertTrue(result?.error is EmailRequiredAuthError)
                verifyZeroInteractions(repository)
            }
    )

    @Test
    fun `sendRecoveryPasswordRequest() email should be valid`() = responseTestCase(
            action = { sendRecoveryPasswordRequest(invalidEmail) },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertTrue(result?.error is MalformedEmailAuthError)
                verifyZeroInteractions(repository)
            }
    )

    @Test
    fun `sendRecoveryPasswordRequest() should handle error on service response AUTH_SERVICE_ERROR`() = responseTestCase(
            setup = { recoverPasswordError = customError },
            action = { sendRecoveryPasswordRequest(validEmail) },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(customError, result?.error)
                verify(repository).recoverPassword(validEmail, authResponse)
                verifyNoMoreInteractions(repository)
            }
    )

    @Test
    fun `sendRecoveryPasswordRequest() should handle success on service response SUCCESS`() = responseTestCase(
            setup = { recoverPasswordError = null /* success */ },
            action = { sendRecoveryPasswordRequest(validEmail) },
            expected = {
                assertEquals(SUCCESS, it?.status)
                verify(repository).recoverPassword(validEmail, authResponse)
                verifyNoMoreInteractions(repository)
            }
    )

}