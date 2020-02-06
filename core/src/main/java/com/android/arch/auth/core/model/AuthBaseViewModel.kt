package com.android.arch.auth.core.model

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.common.extensions.isOnProgress
import com.android.arch.auth.core.common.extensions.postEvent
import com.android.arch.auth.core.common.extensions.setError
import com.android.arch.auth.core.common.extensions.setOnProgress
import com.android.arch.auth.core.data.entity.AuthError
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.Event
import com.android.arch.auth.core.data.repository.AuthRepositoryListener
import com.android.arch.auth.core.domain.auth.AuthResponseListenerUseCase

/**
 * Created by alexk on 11/26/18.
 * Project android-auth-pack
 */
abstract class AuthBaseViewModel<UserProfileDataType>(
    private val authResponseListenerUseCase: AuthResponseListenerUseCase<UserProfileDataType>
) : BaseViewModel() {

    private val _response = MutableLiveData<Event<AuthResponse<UserProfileDataType>>>()

    private val authRepositoryListener: AuthRepositoryListener<UserProfileDataType> =
        object : AuthRepositoryListener<UserProfileDataType> {
            override fun onAuthResponse(response: AuthResponse<UserProfileDataType>) {
                _response.postEvent(response)
            }
        }

    init {
        authResponseListenerUseCase.addListener(authRepositoryListener)
    }

    override fun onCleared() {
        authResponseListenerUseCase.removeListener(authRepositoryListener)
        super.onCleared()
    }

    open val response: LiveData<Event<AuthResponse<UserProfileDataType>>>
        get() = getRawResponseData()

    fun getRawResponseData(): MutableLiveData<Event<AuthResponse<UserProfileDataType>>> {
        return _response
    }

    @MainThread
    protected fun setError(error: AuthError) {
        _response.setError(error)
    }

    @MainThread
    protected fun launchAsyncRequest(query: () -> Unit) {
        if (_response.isOnProgress()) {
            Log.w("LaunchAuthTask:", "Auth task skipped. Actually on progress")
            return
        }

        _response.setOnProgress()
        launchAsync(query)
    }
}