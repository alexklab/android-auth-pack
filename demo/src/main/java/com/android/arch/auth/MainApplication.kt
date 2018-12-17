package com.android.arch.auth

import android.app.Application
import com.android.arch.auth.core.domain.auth.SignInWithEmailUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase
import com.android.arch.auth.core.model.SignInWithEmailViewModel
import com.android.arch.auth.data.database.DatabaseProvider
import com.android.arch.auth.data.repository.FirebaseAuthRepository
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
        single { FirebaseAuthRepository() }
        single { DatabaseProvider() }
    }

    private val viewModelModule = module {
        viewModel {
            SignInWithEmailViewModel(
                SignInWithEmailUseCase(get<FirebaseAuthRepository>()),
                UpdateProfileUseCase(get<DatabaseProvider>())
            )
        }
    }

}

