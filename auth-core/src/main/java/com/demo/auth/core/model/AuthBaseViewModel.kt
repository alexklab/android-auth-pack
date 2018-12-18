package com.demo.auth.core.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.demo.auth.core.common.extensions.isOnProgress
import com.demo.auth.core.common.extensions.postError
import com.demo.auth.core.common.extensions.postOnProgress
import com.demo.auth.core.common.extensions.withIOContext
import com.demo.auth.core.entity.AuthResponse
import com.demo.auth.core.entity.AuthResponseErrorType
import com.demo.auth.core.entity.Event
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