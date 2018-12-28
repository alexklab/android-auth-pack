package com.android.arch.instagram.repository

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object InstagramAuthServiceBuilder {

    fun build(): InstagramAuthService {
        val retrofit = Retrofit.Builder()
            .baseUrl(InstagramAuthService.API_URL)
            .client(OkHttpClient.Builder()
                .readTimeout(InstagramAuthService.HTTP_READ_TIMEOUT_SEC, TimeUnit.SECONDS)
                .connectTimeout(InstagramAuthService.HTTP_CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
                .apply { addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)) }
                .build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(InstagramAuthService::class.java)

    }
}