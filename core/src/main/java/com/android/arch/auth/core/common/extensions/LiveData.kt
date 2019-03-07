package com.android.arch.auth.core.common.extensions

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.data.entity.AuthRequestStatus.FAILED
import com.android.arch.auth.core.data.entity.AuthRequestStatus.ON_PROGRESS
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.entity.AuthResponseError
import com.android.arch.auth.core.data.entity.Event

/**
 * Created by alexk on 12/13/18.
 * Project android-auth-pack
 */
fun <DataType> MutableLiveData<Event<AuthResponse<DataType>>>.postError(error: AuthResponseError) {
    postEvent(AuthResponse(FAILED, error))
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
fun <DataType> MutableLiveData<Event<AuthResponse<DataType>>>.setError(error: AuthResponseError) {
    setEvent(AuthResponse(FAILED, error))
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