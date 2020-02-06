package com.demo.auth.firebase

import androidx.multidex.MultiDexApplication
import com.android.arch.auth.core.domain.auth.*
import com.android.arch.auth.core.domain.profile.DeleteProfileUseCase
import com.android.arch.auth.core.domain.profile.GetProfileUidUseCase
import com.android.arch.auth.core.domain.profile.GetProfileUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase
import com.android.arch.auth.core.model.*
import com.android.arch.auth.facebook.FacebookSignInService
import com.android.arch.auth.firebase.FirebaseAuthRepository
import com.android.arch.auth.google.GoogleSignInService
import com.android.arch.auth.twitter.TwitterSignInService
import com.demo.auth.firebase.common.EmailFieldValidator
import com.demo.auth.firebase.common.LoginFieldValidator
import com.demo.auth.firebase.common.PasswordFieldValidator
import com.demo.auth.firebase.data.database.DatabaseProvider
import com.demo.auth.firebase.data.entity.UserProfile
import com.demo.auth.firebase.ui.UserProfileViewModel
import org.koin.android.ext.android.inject
import org.koin.android.ext.android.startKoin
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

/**
 * Created by alexk on 12/17/18.
 * Project android-auth-pack
 */
class MainApplication : MultiDexApplication() {

    private val dbProvider: DatabaseProvider by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(mainModuleModule, viewModelModule))
        dbProvider.onCreate(this)
    }

    private val mainModuleModule = module {
        single {
            FirebaseAuthRepository(
                ::UserProfile,
                FacebookSignInService(),
                GoogleSignInService(webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID),
                TwitterSignInService(
                    consumerApiKey = BuildConfig.TWITTER_CONSUMER_API_KEY,
                    consumerApiSecretKey = BuildConfig.TWITTER_CONSUMER_API_SECRET_KEY
                )
            )
        }
        single { DatabaseProvider() }
        single { EmailFieldValidator() }
        single { LoginFieldValidator() }
        single { PasswordFieldValidator() }
    }

    private val viewModelModule = module {
        viewModel {
            SignInWithEmailViewModel(
                AuthResponseListenerUseCase(get<FirebaseAuthRepository<UserProfile>>()),
                SignInWithEmailUseCase(get<FirebaseAuthRepository<UserProfile>>()),
                UpdateProfileUseCase(get<DatabaseProvider>())
            )
        }

        viewModel {
            SignInWithSocialNetworksViewModel(
                AuthResponseListenerUseCase(get<FirebaseAuthRepository<UserProfile>>()),
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
                get<EmailFieldValidator>(),
                get<LoginFieldValidator>(),
                get<PasswordFieldValidator>(),
                AuthResponseListenerUseCase(get<FirebaseAuthRepository<UserProfile>>()),
                SignUpUseCase(get<FirebaseAuthRepository<UserProfile>>()),
                UpdateProfileUseCase(get<DatabaseProvider>())
            )
        }

        viewModel {
            RecoveryPasswordViewModel(
                get<EmailFieldValidator>(),
                AuthResponseListenerUseCase(get<FirebaseAuthRepository<UserProfile>>()),
                RecoveryPasswordUseCase(get<FirebaseAuthRepository<UserProfile>>())
            )
        }

        viewModel {
            ChangePasswordViewModel(
                get<PasswordFieldValidator>(),
                AuthResponseListenerUseCase(get<FirebaseAuthRepository<UserProfile>>()),
                ChangePasswordUseCase(get<FirebaseAuthRepository<UserProfile>>()),
                GetProfileUidUseCase(get<DatabaseProvider>())
            )
        }

        viewModel {
            EditProfileViewModel(
                get<EmailFieldValidator>(),
                get<LoginFieldValidator>(),
                AuthResponseListenerUseCase(get<FirebaseAuthRepository<UserProfile>>()),
                SendEditProfileRequestUseCase(get<FirebaseAuthRepository<UserProfile>>()),
                GetProfileUseCase(get<DatabaseProvider>()),
                UpdateProfileUseCase(get<DatabaseProvider>())
            )
        }
    }

}