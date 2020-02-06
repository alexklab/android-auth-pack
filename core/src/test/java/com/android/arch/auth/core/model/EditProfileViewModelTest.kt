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
import com.android.arch.auth.core.data.repository.UserProfileDataCache
import com.android.arch.auth.core.domain.auth.AuthResponseListenerUseCase
import com.android.arch.auth.core.domain.auth.SendEditProfileRequestUseCase
import com.android.arch.auth.core.domain.profile.GetProfileUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase
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

/**
 * Created by alexk on 12/14/18.
 * Project android-auth-pack
 */

class EditProfileViewModelTest :
    AuthBaseViewModelTest<UserProfile, EditProfileViewModel<UserProfile>>() {

    override val instance: EditProfileViewModel<UserProfile>
        get() = EditProfileViewModel(
            emailFieldValidator,
            loginFieldValidator,
            AuthResponseListenerUseCase(repository),
            SendEditProfileRequestUseCase(repository),
            GetProfileUseCase(userProfileDataCache),
            UpdateProfileUseCase(userProfileDataCache)
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
    private lateinit var userProfileDataCache: UserProfileDataCache<UserProfile>
    @Mock
    private lateinit var emailFieldValidator: FieldValidator
    @Mock
    private lateinit var loginFieldValidator: FieldValidator

    private companion object {
        const val LOGIN = "User"
        const val LOGIN_V2 = "User2"
        const val EMAIL = "test@gmail.com"
        const val EMAIL_V2 = "test2@gmail.com"

        const val INVALID_VALUE = "tt"
        const val EMPTY_VALUE = ""
    }

    private var getProfileAnswer: UserProfile? = null
    private var editProfileError: AuthError? = null
    private val customError = ServiceAuthError("Custom error")
    private lateinit var authResponse: MutableLiveData<Event<AuthResponse<UserProfile>>>

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        getProfileAnswer = null
        editProfileError = null

        `when`(repository.editProfile(any())).thenAnswer {
            authResponse.postEvent(editProfileError.toAuthResponse(getProfileAnswer))
        }

        `when`(userProfileDataCache.getProfile()).thenAnswer {
            MutableLiveData<UserProfile>().apply { value = getProfileAnswer }
        }

        `when`(userProfileDataCache.updateProfile(any())).thenAnswer {
            Unit
        }
        `when`(emailFieldValidator.validate(any())).thenAnswer {
            val email = it.arguments[0]
            email == EMAIL || email == EMAIL_V2
        }
        `when`(loginFieldValidator.validate(any())).thenAnswer {
            val login = it.arguments[0]
            login == LOGIN || login == LOGIN_V2
        }
    }

    private fun EditProfileViewModel<UserProfile>.updateProfile(login: String, email: String) {
        sendEditRequest {
            editLogin(LOGIN_V2, login)
            editEmail(EMAIL_V2, email)
        }
    }

    @Test
    fun `updateProfile() login should be NOT empty`() = responseTestCase(
        action = { updateProfile(login = EMPTY_VALUE, email = EMAIL) },
        expected = { result ->
            assertEquals(FAILED, result?.status)
            assertTrue(result?.error is LoginRequiredAuthError)
            verify(repository).addListener(any())
            verifyNoMoreInteractions(repository, userProfileDataCache)
        })

    @Test
    fun `updateProfile() email should be NOT empty`() = responseTestCase(
        action = { updateProfile(login = LOGIN, email = EMPTY_VALUE) },
        expected = { result ->
            assertEquals(FAILED, result?.status)
            assertTrue(result?.error is EmailRequiredAuthError)
            verify(repository).addListener(any())
            verifyNoMoreInteractions(repository, userProfileDataCache)
        })

    @Test
    fun `updateProfile() email should be valid`() = responseTestCase(
        action = { updateProfile(login = LOGIN, email = INVALID_VALUE) },
        expected = { result ->
            assertEquals(FAILED, result?.status)
            assertTrue(result?.error is MalformedEmailAuthError)
            verify(repository).addListener(any())
            verifyNoMoreInteractions(repository, userProfileDataCache)
        })

    @Test
    fun `updateProfile() login should be valid`() = responseTestCase(
        action = { updateProfile(login = INVALID_VALUE, email = EMAIL) },
        expected = { result ->
            assertEquals(FAILED, result?.status)
            assertTrue(result?.error is MalformedLoginAuthError)
            verify(repository).addListener(any())
            verifyNoMoreInteractions(repository, userProfileDataCache)
        })

    @Test
    fun `updateProfile() should handle success on service response SUCCESS`() = responseTestCase(
        setup = { editProfileError = null; getProfileAnswer = UserProfile() /* success */ },
        action = { updateProfile(login = LOGIN, email = EMAIL) },
        expected = { result ->
            assertEquals(SUCCESS, result?.status)
            verify(repository).addListener(any())
            verify(repository).editProfile(any())
            verify(userProfileDataCache).updateProfile(any())
            verifyNoMoreInteractions(repository, userProfileDataCache)
        })

    @Test
    fun `updateProfile() should handle error on service response AUTH_SERVICE_ERROR`() =
        responseTestCase(
            setup = { editProfileError = customError },
            action = { updateProfile(login = LOGIN, email = EMAIL) },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(customError, result?.error)
                verify(repository).addListener(any())
                verify(repository).editProfile(any())
                verifyNoMoreInteractions(repository, userProfileDataCache)
            })

}