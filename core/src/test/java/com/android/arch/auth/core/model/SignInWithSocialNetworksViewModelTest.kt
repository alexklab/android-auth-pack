package com.android.arch.auth.core.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.common.extensions.postEvent
import com.android.arch.auth.core.data.entity.AuthError
import com.android.arch.auth.core.data.entity.AuthError.ServiceAuthError
import com.android.arch.auth.core.data.entity.AuthRequestStatus.FAILED
import com.android.arch.auth.core.data.entity.AuthRequestStatus.SUCCESS
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.Event
import com.android.arch.auth.core.data.entity.SocialNetworkType.GOOGLE
import com.android.arch.auth.core.data.entity.SocialNetworkType.INSTAGRAM
import com.android.arch.auth.core.data.repository.SocialNetworkAuthRepository
import com.android.arch.auth.core.data.repository.UserProfileDataCache
import com.android.arch.auth.core.domain.auth.AuthResponseListenerUseCase
import com.android.arch.auth.core.domain.auth.SignInWithSocialNetworkUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase
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
 * Created by alexk on 12/14/18.
 * Project android-auth-pack
 */
class SignInWithSocialNetworksViewModelTest :
    AuthBaseViewModelTest<UserProfile, SignInWithSocialNetworksViewModel<UserProfile>>() {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a the same thread
    @get:Rule
    var coroutineContextProviderRule = CoroutineContextProviderRule()

    @Mock
    private lateinit var repository: SocialNetworkAuthRepository<UserProfile>
    @Mock
    private lateinit var userProfileDataCache: UserProfileDataCache<UserProfile>

    private lateinit var authResponse: MutableLiveData<Event<AuthResponse<UserProfile>>>

    private var signInWithSocialNetworks: AuthError? = null
    private val customError = ServiceAuthError("Custom Error")

    override val instance: SignInWithSocialNetworksViewModel<UserProfile>
        get() = SignInWithSocialNetworksViewModel(
            AuthResponseListenerUseCase(repository),
            SignInWithSocialNetworkUseCase(repository),
            UpdateProfileUseCase(userProfileDataCache)
        ).apply { authResponse = getRawResponseData() }

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(repository.signInWithSocialNetwork(any())).thenAnswer {
            authResponse.postEvent(signInWithSocialNetworks.toAuthResponse(profile))

        }
    }

    @Test
    fun `signInWithSocialNetwork() should handle error on service response AUTH_SERVICE_ERROR`() =
        responseTestCase(
            setup = { signInWithSocialNetworks = customError },
            action = { signInWithSocialNetwork(INSTAGRAM) },
            expected = { response ->
                assertEquals(FAILED, response?.status)
                assertEquals(customError, response?.error)
                verify(repository).addListener(any())
                verify(repository).signInWithSocialNetwork(INSTAGRAM)
                verifyNoMoreInteractions(repository)
                verifyZeroInteractions(userProfileDataCache)
            })

    @Test
    fun `signInWithSocialNetwork() should handle success on service response SUCCESS`() =
        responseTestCase(
            setup = { signInWithSocialNetworks = null /* success */ },
            action = { signInWithSocialNetwork(GOOGLE) },
            expected = { response ->
                assertEquals(SUCCESS, response?.status)
                verify(repository).addListener(any())
                verify(repository).signInWithSocialNetwork(GOOGLE)
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