package com.android.arch.instagram.repository

import com.android.arch.instagram.data.UserInfoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface InstagramAuthService {

    @GET("/v1/users/self/")
    fun getUserInfo(@Query("access_token") token: String): Call<UserInfoResponse>

    companion object {

        const val API_URL = "https://api.instagram.com"
        const val HTTP_READ_TIMEOUT_SEC = 30L
        const val HTTP_CONNECT_TIMEOUT_SEC = 10L

        fun getAccessTokenUrl(clientId: String, redirectUrl: String) =
            "$API_URL/oauth/authorize/?client_id=$clientId&redirect_uri=$redirectUrl&response_type=token"

    }
}

