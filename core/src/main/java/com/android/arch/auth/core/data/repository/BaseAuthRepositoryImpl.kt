package com.android.arch.auth.core.data.repository

import android.util.Log
import com.android.arch.auth.core.data.entity.AuthError
import com.android.arch.auth.core.data.entity.AuthRequestStatus
import com.android.arch.auth.core.data.entity.AuthResponse
import com.android.arch.auth.core.data.network.OnActivityCreatedListener

abstract class BaseAuthRepositoryImpl<UserProfileDataType> :
    OnActivityCreatedListener(),
    BaseAuthRepository<UserProfileDataType> {

    private val listeners = hashSetOf<AuthRepositoryListener<UserProfileDataType>>()

    override fun addListener(listener: AuthRepositoryListener<UserProfileDataType>) {
        listeners.add(listener)
    }

    override fun removeListener(listener: AuthRepositoryListener<UserProfileDataType>) {
        listeners.remove(listener)
    }

    protected fun postAuthResponse(response: AuthResponse<UserProfileDataType>) {
        if (listeners.isEmpty()) {
            Log.w("$this\$onResult", "Wrong state. Not found SignInResponseListener")
        } else {
            listeners.forEach { it.onAuthResponse(response) }
        }
    }


    protected fun postAuthError(error: AuthError) = postAuthResponse(
        AuthResponse(AuthRequestStatus.FAILED, error = error)
    )

}