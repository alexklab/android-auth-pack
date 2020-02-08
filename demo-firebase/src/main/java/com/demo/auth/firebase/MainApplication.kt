package com.demo.auth.firebase

import android.content.Context
import androidx.multidex.MultiDex
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import timber.log.Timber

/**
 * Created by alexk on 12/17/18.
 * Project android-auth-pack
 */
class MainApplication : DaggerApplication() {

    // private val dbProvider: DatabaseProvider by inject()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        //  startKoin(this, listOf(mainModuleModule, viewModelModule))
        //  dbProvider.onCreate(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return TODO()//..DaggerAppComponent.factory().create(this)
    }
/*
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

 */

}