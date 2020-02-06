package com.android.arch.auth.core.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.common.extensions.postEvent
import com.android.arch.auth.core.data.entity.AuthError
import com.android.arch.auth.core.data.entity.AuthRequestStatus.FAILED
import com.android.arch.auth.core.data.entity.AuthRequestStatus.SUCCESS
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.Event
import com.android.arch.auth.core.data.repository.EmailAuthRepository
import com.android.arch.auth.core.domain.auth.AuthResponseListenerUseCase
import com.android.arch.auth.core.domain.auth.SendVerifiedEmailKeyUseCase
import com.android.arch.auth.core.testutils.CoroutineContextProviderRule
import org.amshove.kluent.any
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Created by alexk on 12/14/18.
 * Project android-auth-pack
 */
class VerifyEmailViewModelTest :
    AuthBaseViewModelTest<UserProfile, VerifyEmailViewModel<UserProfile>>() {

    override val instance: VerifyEmailViewModel<UserProfile>
        get() = VerifyEmailViewModel(
            AuthResponseListenerUseCase(repository),
            SendVerifiedEmailKeyUseCase(repository)
        ).apply {
            authResponse = getRawResponseData()
        }

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a the same thread
    @get:Rule
    var coroutineContextProviderRule = CoroutineContextProviderRule()

    @Mock
    private lateinit var repository: EmailAuthRepository<UserProfile>

    private var verifyEmailError: AuthError? = null
    private val customError = AuthError.ServiceAuthError("Custom Error")
    private lateinit var authResponse: MutableLiveData<Event<AuthResponse<UserProfile>>>

    private val verifyEmailKey = "t123wer123ert"

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        verifyEmailError = null

        `when`(repository.sendVerifiedEmailKeyUseCase(any())).thenAnswer {
            authResponse.postEvent(verifyEmailError.toAuthResponse())
        }
    }

    @Test
    fun `sendVerifyEmailRequest() should post service response SUCCESS`() = responseTestCase(
        setup = { verifyEmailError = null /* success */ },
        action = { sendVerifyEmailRequest(verifyEmailKey) },
        expected = { result ->
            assertEquals(SUCCESS, result?.status)
            verify(repository).addListener(any())
            verify(repository).sendVerifiedEmailKeyUseCase(verifyEmailKey)
            verifyNoMoreInteractions(repository)
        })

    @Test
    fun `sendVerifyEmailRequest() should post service response AUTH_SERVICE_ERROR`() =
        responseTestCase(
            setup = { verifyEmailError = customError },
            action = { sendVerifyEmailRequest(verifyEmailKey) },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(customError, result?.error)
                verify(repository).addListener(any())
                verify(repository).sendVerifiedEmailKeyUseCase(verifyEmailKey)
                verifyNoMoreInteractions(repository)
            })

}