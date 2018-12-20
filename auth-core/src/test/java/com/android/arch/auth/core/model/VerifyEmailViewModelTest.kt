package com.android.arch.auth.core.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.common.extensions.postEvent
import com.android.arch.auth.core.domain.auth.SendVerifiedEmailKeyUseCase
import com.android.arch.auth.core.data.entity.AuthRequestStatus.FAILED
import com.android.arch.auth.core.data.entity.AuthRequestStatus.SUCCESS
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.AuthResponseErrorType
import com.android.arch.auth.core.data.entity.AuthResponseErrorType.AUTH_SERVICE_ERROR
import com.android.arch.auth.core.data.entity.Event
import com.android.arch.auth.core.data.repository.AuthRepository
import com.android.arch.auth.core.testutils.CoroutineContextProviderRule
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.amshove.kluent.any
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * Created by alexk on 12/14/18.
 * Project android-auth-pack
 */
class VerifyEmailViewModelTest : AuthBaseViewModelTest<UserProfile, VerifyEmailViewModel<UserProfile>>() {

    override val instance: VerifyEmailViewModel<UserProfile>
        get() = VerifyEmailViewModel(SendVerifiedEmailKeyUseCase(repository)).apply {
            authResponse = getRawResponseData()
        }

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a the same thread
    @get:Rule
    var coroutineContextProviderRule = CoroutineContextProviderRule()

    @Mock
    private lateinit var repository: AuthRepository<UserProfile>

    private var verifyEmailResponseError: AuthResponseErrorType? = null
    private lateinit var authResponse: MutableLiveData<Event<AuthResponse<UserProfile>>>

    private val verifyEmailKey = "t123wer123ert"

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        verifyEmailResponseError = null

        `when`(repository.sendVerifiedEmailKeyUseCase(any(), any())).thenAnswer {
            (it.arguments.last() as MutableLiveData<Event<AuthResponse<UserProfile>>>)
                    .postEvent(verifyEmailResponseError.toAuthResponse())
        }
    }

    @Test
    fun `sendVerifyEmailRequest() should post service response SUCCESS`() = responseTestCase(
            setup = { verifyEmailResponseError = null /* success */ },
            action = { sendVerifyEmailRequest(verifyEmailKey) },
            expected = { result ->
                assertEquals(SUCCESS, result?.status)
                verify(repository).sendVerifiedEmailKeyUseCase(verifyEmailKey, authResponse)
                verifyNoMoreInteractions(repository)
            })

    @Test
    fun `sendVerifyEmailRequest() should post service response AUTH_SERVICE_ERROR`() = responseTestCase(
            setup = { verifyEmailResponseError = AUTH_SERVICE_ERROR },
            action = { sendVerifyEmailRequest(verifyEmailKey) },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(AUTH_SERVICE_ERROR, result?.errorType)
                verify(repository).sendVerifiedEmailKeyUseCase(verifyEmailKey, authResponse)
                verifyNoMoreInteractions(repository)
            })

}