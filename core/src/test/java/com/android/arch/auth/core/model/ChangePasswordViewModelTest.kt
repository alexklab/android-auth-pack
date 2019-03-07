package com.android.arch.auth.core.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.common.FieldValidator
import com.android.arch.auth.core.common.extensions.postEvent
import com.android.arch.auth.core.domain.auth.ChangePasswordUseCase
import com.android.arch.auth.core.domain.profile.GetProfileUidUseCase
import com.android.arch.auth.core.data.entity.AuthRequestStatus.FAILED
import com.android.arch.auth.core.data.entity.AuthRequestStatus.SUCCESS
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.AuthResponseError
import com.android.arch.auth.core.data.entity.AuthResponseError.*
import com.android.arch.auth.core.data.entity.Event
import com.android.arch.auth.core.data.repository.EmailAuthRepository
import com.android.arch.auth.core.data.repository.UserProfileDataCache
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
 * Created by alexk on 11/21/18.
 * Project android-auth-pack
 */
class ChangePasswordViewModelTest : AuthBaseViewModelTest<UserProfile, ChangePasswordViewModel<UserProfile>>() {

    override val instance: ChangePasswordViewModel<UserProfile>
        get() = ChangePasswordViewModel(
            passwordValidator,
            ChangePasswordUseCase(repository),
            GetProfileUidUseCase(profileDataCache)
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
    private lateinit var profileDataCache: UserProfileDataCache<UserProfile>
    @Mock
    private lateinit var passwordValidator: FieldValidator

    private var changePasswordResponseError: AuthResponseError? = null
    private val customServiceError = ServiceError("Custom cause")
    private lateinit var authResponse: MutableLiveData<Event<AuthResponse<UserProfile>>>


    private val uid = "1nb3vg34jk5l3jeh3k"
    private val emptyPassword = ""
    private val validPassword = "123asdcvf"
    private val invalidPassword = "1234"

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        changePasswordResponseError = null
        `when`(profileDataCache.getProfileUid()).thenReturn(uid)
        `when`(passwordValidator.validate(any())).thenAnswer { it.arguments[0] == validPassword }
        `when`(repository.changePassword(any(), any(), any(), any())).thenAnswer {
            (it.arguments.last() as MutableLiveData<Event<AuthResponse<UserProfile>>>)
                .postEvent(changePasswordResponseError.toAuthResponse())
        }
    }

    @Test
    fun `changePassword() old password should be NOT empty`() = responseTestCase(
        // Given empty old password
        action = {
            changePassword(
                oldPassword = emptyPassword,
                newPassword = validPassword,
                newConfirmPassword = validPassword
            )
        },
        expected = { response ->
            assertEquals(FAILED, response?.status)
            assertEquals(OldPasswordRequired, response?.error)
            verifyZeroInteractions(repository, profileDataCache)
        }
    )

    @Test
    fun `changePassword() new password should be NOT empty`() = responseTestCase(
        // Given empty new password
        action = {
            changePassword(
                oldPassword = validPassword,
                newPassword = emptyPassword,
                newConfirmPassword = emptyPassword
            )
        },
        expected = { response ->
            assertEquals(FAILED, response?.status)
            assertEquals(PasswordRequired, response?.error)
            verifyZeroInteractions(repository, profileDataCache)
        }
    )

    @Test
    fun `changePassword() new password should be valid`() = responseTestCase(
        // Given weak new password
        action = {
            changePassword(
                oldPassword = validPassword,
                newPassword = invalidPassword,
                newConfirmPassword = invalidPassword
            )
        },
        expected = { response ->
            assertEquals(FAILED, response?.status)
            assertEquals(WeakPassword, response?.error)
            verifyZeroInteractions(repository, profileDataCache)
        }
    )

    @Test
    fun `changePassword() confirm password should be NOT empty`() = responseTestCase(
        // Given empty confirm password
        action = {
            changePassword(
                oldPassword = validPassword,
                newPassword = validPassword,
                newConfirmPassword = emptyPassword
            )
        },
        expected = { response ->
            assertEquals(FAILED, response?.status)
            assertEquals(ConfirmPasswordRequired, response?.error)
            verifyZeroInteractions(repository, profileDataCache)
        }
    )

    @Test
    fun `changePassword() confirm password should be equals to new password`() = responseTestCase(
        // Given wrong confirm password
        action = {
            changePassword(
                oldPassword = validPassword,
                newPassword = validPassword,
                newConfirmPassword = invalidPassword
            )
        },
        expected = { response ->
            assertEquals(FAILED, response?.status)
            assertEquals(NotMatchedConfirmPassword, response?.error)
            verifyZeroInteractions(repository, profileDataCache)
        }
    )

    @Test
    fun `changePassword() should handle success on service response SUCCESS`() = responseTestCase(
        // Given correct request params, success changePasswordResponse
        setup = { changePasswordResponseError = null /* success */ },
        action = {
            changePassword(
                oldPassword = validPassword,
                newPassword = validPassword,
                newConfirmPassword = validPassword
            )
        },
        expected = { response ->
            assertEquals(SUCCESS, response?.status)
            verify(profileDataCache).getProfileUid()
            verify(repository).changePassword(uid, validPassword, validPassword, authResponse)
            verifyNoMoreInteractions(repository, profileDataCache)
        }
    )

    @Test
    fun `changePassword() should handle error on service response AUTH_WRONG_PASSWORD`() = responseTestCase(
        // Given correct request params, failed changePasswordResponse
        setup = { changePasswordResponseError = WrongPassword },
        action = {
            changePassword(
                oldPassword = validPassword,
                newPassword = validPassword,
                newConfirmPassword = validPassword
            )
        },
        expected = { response ->
            assertEquals(FAILED, response?.status)
            assertEquals(WrongPassword, response?.error)
            verify(profileDataCache).getProfileUid()
            verify(repository).changePassword(uid, validPassword, validPassword, authResponse)
            verifyNoMoreInteractions(repository, profileDataCache)
        }
    )

    @Test
    fun `changePassword() should handle error on service response AUTH_SERVICE_ERROR`() = responseTestCase(
        // Given correct request params, response: service error
        setup = { changePasswordResponseError = customServiceError },
        action = {
            changePassword(
                oldPassword = validPassword,
                newPassword = validPassword,
                newConfirmPassword = validPassword
            )
        },
        expected = { response ->
            assertEquals(FAILED, response?.status)
            assertEquals(customServiceError, response?.error)
            verify(profileDataCache).getProfileUid()
            verify(repository).changePassword(uid, validPassword, validPassword, authResponse)
            verifyNoMoreInteractions(repository, profileDataCache)
        }
    )

}