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
import com.android.arch.auth.core.domain.auth.SendEditProfileRequestUseCase
import com.android.arch.auth.core.domain.profile.GetProfileUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase
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

/**
 * Created by alexk on 12/14/18.
 * Project android-auth-pack
 */

class EditProfileViewModelTest : AuthBaseViewModelTest<UserProfile, EditProfileViewModel<UserProfile>>() {

    override val instance: EditProfileViewModel<UserProfile>
        get() = EditProfileViewModel(
            emailFieldValidator,
            loginFieldValidator,
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

        `when`(repository.editProfile(any(), any())).thenAnswer {
            (it.arguments.last() as MutableLiveData<Event<AuthResponse<UserProfile>>>)
                .postEvent(editProfileError.toAuthResponse(getProfileAnswer))
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

    private fun EditProfileViewModel<UserProfile>.updateProfile(login:String, email:String){
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
            assertEquals(LoginRequiredAuthError, result?.error)
            verifyZeroInteractions(repository, userProfileDataCache)
        })

    @Test
    fun `updateProfile() email should be NOT empty`() = responseTestCase(
        action = { updateProfile(login = LOGIN, email = EMPTY_VALUE) },
        expected = { result ->
            assertEquals(FAILED, result?.status)
            assertEquals(EmailRequiredAuthError, result?.error)
            verifyZeroInteractions(repository, userProfileDataCache)
        })

    @Test
    fun `updateProfile() email should be valid`() = responseTestCase(
        action = { updateProfile(login = LOGIN, email = INVALID_VALUE) },
        expected = { result ->
            assertEquals(FAILED, result?.status)
            assertEquals(MalformedEmailAuthError, result?.error)
            verifyZeroInteractions(repository, userProfileDataCache)
        })

    @Test
    fun `updateProfile() login should be valid`() = responseTestCase(
        action = { updateProfile(login = INVALID_VALUE, email = EMAIL) },
        expected = { result ->
            assertEquals(FAILED, result?.status)
            assertEquals(MalformedLoginAuthError, result?.error)
            verifyZeroInteractions(repository, userProfileDataCache)
        })

    @Test
    fun `updateProfile() login should be auth already, on service response AUTH_LOGIN_ALREADY_EXIST`() =
        responseTestCase(
            setup = { editProfileError = LoginAlreadyExistAuthError },
            action = { updateProfile(login = LOGIN, email = EMAIL) },
            expected = { result ->
                assertEquals(FAILED, result?.status)
                assertEquals(LoginAlreadyExistAuthError, result?.error)
                verify(repository).editProfile(any(), any())
                verifyNoMoreInteractions(repository, userProfileDataCache)
            })

    @Test
    fun `updateProfile() should handle success on service response SUCCESS`() = responseTestCase(
        setup = { editProfileError = null; getProfileAnswer = UserProfile() /* success */ },
        action = { updateProfile(login = LOGIN, email = EMAIL) },
        expected = { result ->
            assertEquals(SUCCESS, result?.status)
            verify(repository).editProfile(any(), any())
            verify(userProfileDataCache).updateProfile(any())
            verifyNoMoreInteractions(repository, userProfileDataCache)
        })

    @Test
    fun `updateProfile() should handle error on service response AUTH_SERVICE_ERROR`() = responseTestCase(
        setup = { editProfileError = customError },
        action = { updateProfile(login = LOGIN, email = EMAIL) },
        expected = { result ->
            assertEquals(FAILED, result?.status)
            assertEquals(customError, result?.error)
            verify(repository).editProfile(any(), any())
            verifyNoMoreInteractions(repository, userProfileDataCache)
        })

}