package com.demo.auth.core.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.demo.auth.core.common.extensions.postEvent
import com.demo.auth.core.domain.auth.SignInWithSocialNetworkUseCase
import com.demo.auth.core.domain.profile.UpdateProfileUseCase
import com.demo.auth.core.entity.AuthRequestStatus.FAILED
import com.demo.auth.core.entity.AuthRequestStatus.SUCCESS
import com.demo.auth.core.entity.AuthResponse
import com.demo.auth.core.entity.AuthResponseErrorType
import com.demo.auth.core.entity.AuthResponseErrorType.AUTH_CANCELED
import com.demo.auth.core.entity.AuthResponseErrorType.AUTH_SERVICE_ERROR
import com.demo.auth.core.entity.Event
import com.demo.auth.core.entity.SocialNetworkType.*
import com.demo.auth.core.repos.AuthRepository
import com.demo.auth.core.repos.UserProfileDataCache
import com.demo.auth.core.testutils.CoroutineContextProviderRule
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
 * Created by alexk on 12/14/18.
 * Project android-auth-pack
 */
class SignInWithSocialNetworksViewModelTest : AuthBaseViewModelTest<UserProfile, SignInWithSocialNetworksViewModel<UserProfile>>() {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a the same thread
    @get:Rule
    var coroutineContextProviderRule = CoroutineContextProviderRule()

    @Mock
    private lateinit var repository: AuthRepository<UserProfile>
    @Mock
    private lateinit var userProfileDataCache: UserProfileDataCache<UserProfile>

    private lateinit var authResponse: MutableLiveData<Event<AuthResponse<UserProfile>>>

    private var signInWithSocialNetworksResponse: AuthResponseErrorType? = null

    override val instance: SignInWithSocialNetworksViewModel<UserProfile>
        get() = SignInWithSocialNetworksViewModel(
                SignInWithSocialNetworkUseCase(repository),
                UpdateProfileUseCase(userProfileDataCache)
        ).apply { authResponse = getRawResponseData() }

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(repository.signInWithSocialNetwork(any(), any())).thenAnswer {
            (it.arguments.last() as MutableLiveData<Event<AuthResponse<UserProfile>>>)
                    .postEvent(signInWithSocialNetworksResponse.toAuthResponse(profile))

        }
    }

    @Test
    fun `signInWithSocialNetwork() should handle error on service response AUTH_CANCELED`() = responseTestCase(
            setup = { signInWithSocialNetworksResponse = AUTH_CANCELED },
            action = { signInWithSocialNetwork(FACEBOOK) },
            expected = { response ->
                assertEquals(FAILED, response?.status)
                assertEquals(AUTH_CANCELED, response?.errorType)
                verify(repository).signInWithSocialNetwork(FACEBOOK, authResponse)
                verifyNoMoreInteractions(repository)
                verifyZeroInteractions(userProfileDataCache)
            })

    @Test
    fun `signInWithSocialNetwork() should handle error on service response AUTH_SERVICE_ERROR`() = responseTestCase(
            setup = { signInWithSocialNetworksResponse = AUTH_SERVICE_ERROR },
            action = { signInWithSocialNetwork(INSTAGRAM) },
            expected = { response ->
                assertEquals(FAILED, response?.status)
                assertEquals(AUTH_SERVICE_ERROR, response?.errorType)
                verify(repository).signInWithSocialNetwork(INSTAGRAM, authResponse)
                verifyNoMoreInteractions(repository)
                verifyZeroInteractions(userProfileDataCache)
            })

    @Test
    fun `signInWithSocialNetwork() should handle success on service response SUCCESS`() = responseTestCase(
            setup = { signInWithSocialNetworksResponse = null /* success */ },
            action = { signInWithSocialNetwork(GOOGLE) },
            expected = { response ->
                assertEquals(SUCCESS, response?.status)
                verify(repository).signInWithSocialNetwork(GOOGLE, authResponse)
                verify(userProfileDataCache).updateProfile(profile)
                verifyNoMoreInteractions(repository, userProfileDataCache)
            })

    private companion object {
        private val profile = UserProfile(
                login = "user_name",
                email = "user@mail.com",
                uid = UUID.randomUUID().toString()
        )
    }
}