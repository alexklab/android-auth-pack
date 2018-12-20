package com.demo.auth.firebase

import android.app.Application
import com.demo.auth.core.domain.auth.SignInWithEmailUseCase
import com.demo.auth.core.domain.auth.SignInWithSocialNetworkUseCase
import com.demo.auth.core.domain.profile.UpdateProfileUseCase
import com.demo.auth.core.model.SignInWithEmailViewModel
import com.demo.auth.core.model.SignInWithSocialNetworksViewModel
import com.demo.auth.firebase.data.database.DatabaseProvider
import com.demo.auth.firebase.data.entity.UserProfile
import com.demo.auth.firebase.data.network.FacebookSignInService
import com.demo.auth.firebase.data.network.GoogleSignInService
import com.demo.auth.firebase.data.repository.FirebaseAuthRepository
import com.demo.auth.firebase.data.repository.FirebaseUserProfileFactory
import org.koin.android.ext.android.startKoin
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

/**
 * Created by alexk on 12/17/18.
 * Project android-auth-pack
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(mainModuleModule, viewModelModule))
    }

    private val mainModuleModule = module {
        single { FirebaseUserProfileFactory() }
        single { FirebaseAuthRepository(get<FirebaseUserProfileFactory>()) }
        single { DatabaseProvider() }
    }

    private val viewModelModule = module {
        viewModel {
            SignInWithEmailViewModel(
                SignInWithEmailUseCase(get<FirebaseAuthRepository<UserProfile>>()),
                UpdateProfileUseCase(get<DatabaseProvider>())
            )
        }

        viewModel {
            SignInWithSocialNetworksViewModel(
                SignInWithSocialNetworkUseCase(get<FirebaseAuthRepository<UserProfile>>()),
                UpdateProfileUseCase(get<DatabaseProvider>())
            )
        }
    }

}