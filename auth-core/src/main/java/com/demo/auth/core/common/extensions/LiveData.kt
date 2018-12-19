package com.demo.auth.core.common.extensions

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import com.demo.auth.core.entity.AuthRequestStatus.FAILED
import com.demo.auth.core.entity.AuthRequestStatus.ON_PROGRESS
import com.demo.auth.core.entity.AuthResponse
import com.demo.auth.core.entity.AuthResponseErrorType
import com.demo.auth.core.entity.Event

/**
 * Created by alexk on 12/13/18.
 * Project android-auth-pack
 */
fun <DataType> MutableLiveData<Event<AuthResponse<DataType>>>.postError(
    errorType: AuthResponseErrorType,
    errorMessage: String? = null
) {
    postEvent(AuthResponse(FAILED, errorType = errorType, errorMessage = errorMessage))
}

fun <DataType> MutableLiveData<Event<AuthResponse<DataType>>>.postOnProgress() {
    postEvent(AuthResponse(ON_PROGRESS))
}

fun <DataType> MutableLiveData<Event<AuthResponse<DataType>>>.isOnProgress(): Boolean =
    value?.peekContent()?.status == ON_PROGRESS

fun <DataType> MutableLiveData<Event<AuthResponse<DataType>>>.postEvent(response: AuthResponse<DataType>) {
    postValue(Event(response))
}

@MainThread
fun <DataType> MutableLiveData<Event<AuthResponse<DataType>>>.setError(
    errorType: AuthResponseErrorType,
    errorMessage: String? = null
) {
    setEvent(AuthResponse(FAILED, errorType = errorType, errorMessage = errorMessage))
}

@MainThread
fun <DataType> MutableLiveData<Event<AuthResponse<DataType>>>.setOnProgress() {
    setEvent(AuthResponse(ON_PROGRESS))
}

@MainThread
fun <DataType> MutableLiveData<Event<AuthResponse<DataType>>>.setEvent(response: AuthResponse<DataType>) {
    value = Event(response)
}

fun <DataType> Event<AuthResponse<DataType>>.applyOnSuccess(action: (DataType) -> Unit): Event<AuthResponse<DataType>> {
    val response = peekContent()
    if (response.isSuccess()) {
        response.data
            ?.let { action(it) }
            ?: Log.w("applyOnSuccess:", "Undefined profile")
    }
    return this
}