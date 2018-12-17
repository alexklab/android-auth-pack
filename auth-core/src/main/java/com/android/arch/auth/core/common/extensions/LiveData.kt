package com.android.arch.auth.core.common.extensions

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import com.android.arch.auth.core.entity.AuthRequestStatus.FAILED
import com.android.arch.auth.core.entity.AuthRequestStatus.ON_PROGRESS
import com.android.arch.auth.core.entity.AuthResponse
import com.android.arch.auth.core.entity.AuthResponseErrorType
import com.android.arch.auth.core.entity.Event

/**
 * Created by alexk on 12/13/18.
 * Project android-auth-pack
 */
@MainThread
fun <DataType> MutableLiveData<Event<AuthResponse<DataType>>>.postError(errorType: AuthResponseErrorType) {
    value = Event(AuthResponse(FAILED, errorType = errorType))
}

@MainThread
fun <DataType> MutableLiveData<Event<AuthResponse<DataType>>>.postOnProgress() {
    value = Event(AuthResponse(ON_PROGRESS))
}

fun <DataType> MutableLiveData<Event<AuthResponse<DataType>>>.isOnProgress(): Boolean =
        value?.peekContent()?.status == ON_PROGRESS

fun <DataType> MutableLiveData<Event<AuthResponse<DataType>>>.postEvent(response: AuthResponse<DataType>) {
    postValue(Event(response))
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