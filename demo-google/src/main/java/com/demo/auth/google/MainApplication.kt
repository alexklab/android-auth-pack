package com.demo.auth.google

import android.app.Application
import com.android.arch.auth.core.domain.auth.NetworksSignOutUseCase
import com.android.arch.auth.core.domain.auth.SignInWithSocialNetworkUseCase
import com.android.arch.auth.core.domain.profile.DeleteProfileUseCase
import com.android.arch.auth.core.domain.profile.GetProfileUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase
import com.android.arch.auth.core.model.SignInWithSocialNetworksViewModel
import com.android.arch.auth.google.GoogleSignInService
import com.demo.auth.google.database.DatabaseProvider
import com.demo.auth.google.repo.AuthRepository
import com.demo.auth.google.ui.UserProfileViewModel
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
        single { DatabaseProvider() }
        single { AuthRepository(GoogleSignInService(getString(R.string.google_web_client_id))) }
    }

    private val viewModelModule = module {

        viewModel {
            SignInWithSocialNetworksViewModel(
                SignInWithSocialNetworkUseCase(get<AuthRepository>()),
                UpdateProfileUseCase(get<DatabaseProvider>())
            )
        }

        viewModel {
            UserProfileViewModel(
                GetProfileUseCase(get<DatabaseProvider>()),
                NetworksSignOutUseCase(get<AuthRepository>()),
                DeleteProfileUseCase(get<DatabaseProvider>())
            )
        }
    }

}