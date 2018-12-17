package com.android.arch.auth.core.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.common.FieldValidator
import com.android.arch.auth.core.common.extensions.postEvent
import com.android.arch.auth.core.domain.auth.RecoveryPasswordUseCase
import com.android.arch.auth.core.entity.AuthRequestStatus.FAILED
import com.android.arch.auth.core.entity.AuthRequestStatus.SUCCESS
import com.android.arch.auth.core.entity.AuthResponse
import com.android.arch.auth.core.entity.AuthResponseErrorType
import com.android.arch.auth.core.entity.AuthResponseErrorType.*
import com.android.arch.auth.core.entity.Event
import com.android.arch.auth.core.repos.AuthRepository
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

class RecoveryPasswordViewModelTest : AuthBaseViewModelTest<UserProfile, RecoveryPasswordViewModel<UserProfile>>() {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a the same thread
    @get:Rule
    var coroutineContextProviderRule = CoroutineContextProviderRule()

    @Mock
    private lateinit var repository: AuthRepository<UserProfile>
    @Mock
    private lateinit var emailValidator: FieldValidator

    private lateinit var authResponse: MutableLiveData<Event<AuthResponse<UserProfile>>>

    private var recoverPasswordResponseError: AuthResponseErrorType? = null

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
        recoverPasswordResponseError = null
        `when`(emailValidator.validate(any())).thenAnswer { it.arguments[0] == validEmail }
        `when`(repository.recoverPassword(any(), any())).thenAnswer {
            (it.arguments.last() as MutableLiveData<Event<AuthResponse<UserProfile>>>)
                    .postEvent(recoverPasswordResponseError.toAuthResponse())
        }
    }

    @Test
    fun `sendRecoveryPasswordRequest() email should be NOT empty`() = responseTestCase(
            action = { sendRecoveryPasswordRequest(emptyEmail) },
            expected = { result ->
                assertEquals(EMPTY_EMAIL, result?.errorType)
                assertEquals(FAILED, result?.status)
                verifyZeroInteractions(repository)
            }
    )

    @Test
    fun `sendRecoveryPasswordRequest() email should be valid`() = responseTestCase(
            action = { sendRecoveryPasswordRequest(invalidEmail) },
            expected = { result ->
                assertEquals(INVALID_EMAIL, result?.errorType)
                assertEquals(FAILED, result?.status)
                verifyZeroInteractions(repository)
            }
    )

    @Test
    fun `sendRecoveryPasswordRequest() should handle error on service response AUTH_SERVICE_ERROR`() = responseTestCase(
            setup = { recoverPasswordResponseError = AUTH_SERVICE_ERROR },
            action = { sendRecoveryPasswordRequest(validEmail) },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(AUTH_SERVICE_ERROR, result?.errorType)
                verify(repository).recoverPassword(validEmail, authResponse)
                verifyNoMoreInteractions(repository)
            }
    )

    @Test
    fun `sendRecoveryPasswordRequest() should handle error on service response AUTH_ACCOUNT_NOT_FOUND`() = responseTestCase(
            setup = { recoverPasswordResponseError = AUTH_ACCOUNT_NOT_FOUND },
            action = { sendRecoveryPasswordRequest(validEmail) },
            expected = { result ->
                assertEquals(AUTH_ACCOUNT_NOT_FOUND, result?.errorType)
                assertEquals(FAILED, result?.status)
                verify(repository).recoverPassword(validEmail, authResponse)
                verifyNoMoreInteractions(repository)
            }
    )

    @Test
    fun `sendRecoveryPasswordRequest() should handle success on service response SUCCESS`() = responseTestCase(
            setup = { recoverPasswordResponseError = null /* success */ },
            action = { sendRecoveryPasswordRequest(validEmail) },
            expected = {
                assertEquals(SUCCESS, it?.status)
                verify(repository).recoverPassword(validEmail, authResponse)
                verifyNoMoreInteractions(repository)
            }
    )

}