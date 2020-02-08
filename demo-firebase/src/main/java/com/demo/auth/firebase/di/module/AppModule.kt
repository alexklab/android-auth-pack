/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.demo.auth.firebase.di.module

import android.content.Context
import com.android.arch.auth.facebook.FacebookSignInService
import com.android.arch.auth.firebase.FirebaseAuthRepository
import com.android.arch.auth.google.GoogleSignInService
import com.android.arch.auth.twitter.TwitterSignInService
import com.demo.auth.firebase.BuildConfig
import com.demo.auth.firebase.MainApplication
import com.demo.auth.firebase.db.AppDatabase
import com.demo.auth.firebase.db.entity.UserProfile
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Defines all the classes that need to be provided in the scope of the app.
 *
 * Define here all objects that are shared throughout the app, like dataBase
 * If some of those objects are singletons, they should be annotated with `@Singleton`.
 */
@Module
class AppModule {

    @Provides
    fun provideContext(application: MainApplication): Context = application.applicationContext

    @Singleton
    @Provides
    fun providesAppDatabase(context: Context): AppDatabase = AppDatabase.getInstance(context)


    @Singleton
    @Provides
    fun provideFirebaseAuthRepository(): FirebaseAuthRepository<UserProfile> =
        FirebaseAuthRepository(
            ::UserProfile,
            FacebookSignInService(),
            GoogleSignInService(BuildConfig.GOOGLE_WEB_CLIENT_ID),
            TwitterSignInService(
                consumerApiKey = BuildConfig.TWITTER_CONSUMER_API_KEY,
                consumerApiSecretKey = BuildConfig.TWITTER_CONSUMER_API_SECRET_KEY
            )
        )


}
