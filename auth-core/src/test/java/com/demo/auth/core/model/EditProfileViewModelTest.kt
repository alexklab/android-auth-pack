package com.demo.auth.core.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.demo.auth.core.common.FieldValidator
import com.demo.auth.core.common.extensions.postEvent
import com.demo.auth.core.domain.auth.SendUpdateProfileRequestUseCase
import com.demo.auth.core.domain.profile.GetProfileUidUseCase
import com.demo.auth.core.entity.AuthRequestStatus.FAILED
import com.demo.auth.core.entity.AuthRequestStatus.SUCCESS
import com.demo.auth.core.entity.AuthResponse
import com.demo.auth.core.entity.AuthResponseErrorType
import com.demo.auth.core.entity.AuthResponseErrorType.*
import com.demo.auth.core.entity.Event
import com.demo.auth.core.repos.AuthRepository
import com.demo.auth.core.repos.UserProfileDataCache
import com.demo.auth.core.testutils.CoroutineContextProviderRule
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

/**
 * Created by alexk on 12/14/18.
 * Project android-auth-pack
 */

class EditProfileViewModelTest : AuthBaseViewModelTest<UserProfile, EditProfileViewModel<UserProfile>>() {

    override val instance: EditProfileViewModel<UserProfile>
        get() = EditProfileViewModel(
                emailFieldValidator,
                loginFieldValidator,
                SendUpdateProfileRequestUseCase(repository),
                GetProfileUidUseCase(userProfileDataCache)
        ).apply { authResponse = getRawResponseData() }

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
    @Mock
    private lateinit var emailFieldValidator: FieldValidator
    @Mock
    private lateinit var loginFieldValidator: FieldValidator

    private companion object {
        const val UID = "user_uid"
        const val LOGIN = "User"
        const val LOGIN_V2 = "User2"
        const val EMAIL = "test@gmail.com"
        const val EMAIL_V2 = "test2@gmail.com"

        const val INVALID_VALUE = "tt"
        const val EMPTY_VALUE = ""
    }

    private var getProfileAnswer: UserProfile? = null
    private var editProfileResponseError: AuthResponseErrorType? = null
    private lateinit var authResponse: MutableLiveData<Event<AuthResponse<UserProfile>>>

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        getProfileAnswer = null
        editProfileResponseError = null

        `when`(repository.sendUpdateProfileRequest(any(), any(), any(), any())).thenAnswer {
            (it.arguments.last() as MutableLiveData<Event<AuthResponse<UserProfile>>>)
                    .postEvent(editProfileResponseError.toAuthResponse())
        }

        `when`(userProfileDataCache.getProfile()).thenAnswer {
            MutableLiveData<UserProfile>().apply { value = getProfileAnswer }
        }

        `when`(userProfileDataCache.getProfileUid()).thenReturn(UID)
        `when`(emailFieldValidator.validate(any())).thenAnswer {
            val email = it.arguments[0]
            email == EMAIL || email == EMAIL_V2
        }
        `when`(loginFieldValidator.validate(any())).thenAnswer {
            val login = it.arguments[0]
            login == LOGIN || login == LOGIN_V2
        }
    }

    @Test
    fun `updateProfile() login should be NOT empty`() = responseTestCase(
            action = { updateProfile(login = EMPTY_VALUE, email = EMAIL) },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(EMPTY_LOGIN, result?.errorType)
                verifyZeroInteractions(repository, userProfileDataCache)
            })

    @Test
    fun `updateProfile() email should be NOT empty`() = responseTestCase(
            action = { updateProfile(login = LOGIN, email = EMPTY_VALUE) },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(EMPTY_EMAIL, result?.errorType)
                verifyZeroInteractions(repository, userProfileDataCache)
            })

    @Test
    fun `updateProfile() email should be valid`() = responseTestCase(
            action = { updateProfile(login = LOGIN, email = INVALID_VALUE) },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(INVALID_EMAIL, result?.errorType)
                verifyZeroInteractions(repository, userProfileDataCache)
            })

    @Test
    fun `updateProfile() login should be valid`() = responseTestCase(
            action = { updateProfile(login = INVALID_VALUE, email = EMAIL) },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(INVALID_LOGIN, result?.errorType)
                verifyZeroInteractions(repository, userProfileDataCache)
            })

    @Test
    fun `updateProfile() login should be auth already, on service response AUTH_LOGIN_ALREADY_EXIST`() = responseTestCase(
            setup = { editProfileResponseError = AUTH_LOGIN_ALREADY_EXIST },
            action = { updateProfile(login = LOGIN, email = EMAIL) },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(AUTH_LOGIN_ALREADY_EXIST, result?.errorType)
                verify(userProfileDataCache).getProfileUid()
                verify(repository).sendUpdateProfileRequest(UID, LOGIN, EMAIL, authResponse)
                verifyNoMoreInteractions(repository, userProfileDataCache)
            })

    @Test
    fun `updateProfile() should handle success on service response SUCCESS`() = responseTestCase(
            setup = { editProfileResponseError = null /* success */ },
            action = { updateProfile(login = LOGIN, email = EMAIL) },
            expected = { result ->
                assertEquals(SUCCESS, result?.status)
                verify(userProfileDataCache).getProfileUid()
                verify(repository).sendUpdateProfileRequest(UID, LOGIN, EMAIL, authResponse)
                verifyNoMoreInteractions(repository, userProfileDataCache)
            })

    @Test
    fun `updateProfile() should handle error on service response AUTH_SERVICE_ERROR`() = responseTestCase(
            setup = { editProfileResponseError = AUTH_SERVICE_ERROR },
            action = { updateProfile(login = LOGIN, email = EMAIL) },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(AUTH_SERVICE_ERROR, result?.errorType)
                verify(userProfileDataCache).getProfileUid()
                verify(repository).sendUpdateProfileRequest(UID, LOGIN, EMAIL, authResponse)
                verifyNoMoreInteractions(repository, userProfileDataCache)
            })

}