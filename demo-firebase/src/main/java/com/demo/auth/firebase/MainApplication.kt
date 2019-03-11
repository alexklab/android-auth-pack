package com.demo.auth.firebase

import android.app.Application
import com.android.arch.auth.core.domain.auth.NetworksSignOutUseCase
import com.android.arch.auth.core.domain.auth.SignInWithEmailUseCase
import com.android.arch.auth.core.domain.auth.SignInWithSocialNetworkUseCase
import com.android.arch.auth.core.domain.auth.SignUpUseCase
import com.android.arch.auth.core.domain.profile.DeleteProfileUseCase
import com.android.arch.auth.core.domain.profile.GetProfileUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase
import com.android.arch.auth.core.model.SignInWithEmailViewModel
import com.android.arch.auth.core.model.SignInWithSocialNetworksViewModel
import com.android.arch.auth.core.model.SignUpViewModel
import com.android.arch.auth.firebase.FirebaseAuthRepository
import com.demo.auth.firebase.common.EmailFieldValidator
import com.demo.auth.firebase.common.LoginFieldValidator
import com.demo.auth.firebase.common.PasswordFieldValidator
import com.demo.auth.firebase.data.database.DatabaseProvider
import com.demo.auth.firebase.data.entity.FirebaseUserProfileFactory
import com.demo.auth.firebase.data.entity.UserProfile
import com.demo.auth.firebase.ui.UserProfileViewModel
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

        viewModel {
            UserProfileViewModel(
                GetProfileUseCase(get<DatabaseProvider>()),
                NetworksSignOutUseCase(get<FirebaseAuthRepository<UserProfile>>()),
                DeleteProfileUseCase(get<DatabaseProvider>())
            )
        }

        viewModel {
            SignUpViewModel(
                EmailFieldValidator(),
                LoginFieldValidator(),
                PasswordFieldValidator(),
                SignUpUseCase(get<FirebaseAuthRepository<UserProfile>>()),
                UpdateProfileUseCase(get<DatabaseProvider>())
            )
        }
    }

}