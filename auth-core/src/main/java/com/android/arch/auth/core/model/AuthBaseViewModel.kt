package com.android.arch.auth.core.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.common.extensions.isOnProgress
import com.android.arch.auth.core.common.extensions.postError
import com.android.arch.auth.core.common.extensions.postOnProgress
import com.android.arch.auth.core.common.extensions.withIOContext
import com.android.arch.auth.core.entity.AuthResponse
import com.android.arch.auth.core.entity.AuthResponseErrorType
import com.android.arch.auth.core.entity.Event
import kotlinx.coroutines.launch

/**
 * Created by alexk on 11/26/18.
 * Project android-auth-pack
 */
abstract class AuthBaseViewModel<UserProfileDataType> : BaseViewModel() {

    private val _response = MutableLiveData<Event<AuthResponse<UserProfileDataType>>>()

    open val response: LiveData<Event<AuthResponse<UserProfileDataType>>>
        get() = getRawResponseData()

    fun getRawResponseData(): MutableLiveData<Event<AuthResponse<UserProfileDataType>>> {
        return _response
    }

    protected fun postError(errorType: AuthResponseErrorType) {
        _response.postError(errorType)
    }

    protected fun launchAuthTask(task: (response: MutableLiveData<Event<AuthResponse<UserProfileDataType>>>) -> Unit) {
        if (_response.isOnProgress()) {
            Log.w("LaunchAuthTask:", "Auth task skipped. Actually on progress")
            return
        }

        _response.postOnProgress()
        uiScope.launch {
            withIOContext {
                task(_response)
            }
        }
    }
}