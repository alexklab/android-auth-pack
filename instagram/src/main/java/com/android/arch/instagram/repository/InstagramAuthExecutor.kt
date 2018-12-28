package com.android.arch.instagram.repository

import android.util.Log
import com.android.arch.auth.core.common.CoroutineContextProvider
import com.android.arch.instagram.data.InstagramUserAccount
import com.android.arch.instagram.data.UserInfoResponse
import com.android.arch.instagram.repository.AuthException.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Created by alexk on 12/27/18.
 * Project android-auth-pack
 */
class InstagramAuthExecutor {

    /**
     * Cancel all coroutines when the InstagramAuthExecutor is canceled
     */
    fun onCanceled() {
        executionJob.cancel()
    }

    /**
     * Fetch Instagram user Info
     */
    fun getUserInfo(token: String, callback: (InstagramUserAccount?, Exception?) -> Unit) {
        uiScope.launch {
            val (response, e) = withContext(CoroutineContextProvider.IO) {
                try {
                    val response = InstagramAuthServiceBuilder.build()
                        .getUserInfo(token)
                        .execute()
                    getAccount(response) to null
                } catch (exception: Exception) {
                    null to exception
                }
            }

            callback(response, e)
        }
    }

    private fun getAccount(response: Response<UserInfoResponse>) = with(response) {
        if (isSuccessful) {
            Log.d("getUserInfo", "Response: ${body()}")
            body()?.account ?: throw NullResponseBodyApiException("UserInfoResponse account is null")
        } else {
            throw FailedResponseApiException("Response failed. Code '${code()}'. ${errorBody()?.string()}")
        }
    }

    /**
     * This is the job for all coroutines started by this ViewModel.
     *
     * Cancelling this job will cancel all coroutines started by this InstagramAuthExecutor.
     */
    private val executionJob = Job()

    /**
     * This is the main scope for all coroutines launched by InstagramAuthExecutor.
     *
     * Since we pass executionJob, you can cancel all coroutines launched by uiScope by calling
     * executionJob.cancel()
     */
    private val uiScope = CoroutineScope(CoroutineContextProvider.Main + executionJob)

}