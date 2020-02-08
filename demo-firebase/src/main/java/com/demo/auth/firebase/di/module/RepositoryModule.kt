package com.demo.auth.firebase.di.module


import com.android.arch.auth.core.common.EmailFieldValidator
import com.android.arch.auth.core.common.LoginFieldValidator
import com.android.arch.auth.core.common.PasswordFieldValidator
import com.android.arch.auth.core.data.repository.BaseAuthRepository
import com.android.arch.auth.core.data.repository.EmailAuthRepository
import com.android.arch.auth.core.data.repository.SocialNetworkAuthRepository
import com.android.arch.auth.core.data.repository.UserProfileDataCache
import com.android.arch.auth.core.domain.auth.*
import com.android.arch.auth.core.domain.profile.DeleteProfileUseCase
import com.android.arch.auth.core.domain.profile.GetProfileUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase
import com.android.arch.auth.firebase.FirebaseAuthRepository
import com.demo.auth.firebase.common.EmailFieldValidatorImpl
import com.demo.auth.firebase.common.LoginFieldValidatorImpl
import com.demo.auth.firebase.common.PasswordFieldValidatorImpl
import com.demo.auth.firebase.db.AppDatabase
import com.demo.auth.firebase.db.UserProfileRepository
import com.demo.auth.firebase.db.entity.UserProfile
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepositoryModule {

    @Singleton
    @Provides
    fun provideUserProfileDataCache(database: AppDatabase): UserProfileDataCache<UserProfile> {
        return UserProfileRepository(database.dao())
    }

    @Singleton
    @Provides
    fun provideBaseAuthRepository(repository: FirebaseAuthRepository<UserProfile>): BaseAuthRepository<UserProfile> {
        return repository
    }

    @Singleton
    @Provides
    fun provideEmailAuthRepository(repository: FirebaseAuthRepository<UserProfile>): EmailAuthRepository<UserProfile> {
        return repository
    }

    @Singleton
    @Provides
    fun provideSocialNetworkAuthRepository(repository: FirebaseAuthRepository<UserProfile>): SocialNetworkAuthRepository<UserProfile> {
        return repository
    }

    // FieldValidators
    @Provides
    fun provideEmailFieldValidator(): EmailFieldValidator = EmailFieldValidatorImpl()

    @Provides
    fun provideLoginFieldValidator(): LoginFieldValidator = LoginFieldValidatorImpl()

    @Provides
    fun providePasswordFieldValidator(): PasswordFieldValidator = PasswordFieldValidatorImpl()

    // UseCases

    @Singleton
    @Provides
    fun provideGetProfileUseCase(dataCache: UserProfileDataCache<UserProfile>):
            GetProfileUseCase<UserProfile> = GetProfileUseCase(dataCache)

    @Singleton
    @Provides
    fun provideUpdateProfileUseCase(dataCache: UserProfileDataCache<UserProfile>):
            UpdateProfileUseCase<UserProfile> = UpdateProfileUseCase(dataCache)

    @Singleton
    @Provides
    fun provideDeleteProfileUseCase(dataCache: UserProfileDataCache<UserProfile>):
            DeleteProfileUseCase<UserProfile> = DeleteProfileUseCase(dataCache)

    @Singleton
    @Provides
    fun provideNetworksSignOutUseCase(repository: SocialNetworkAuthRepository<UserProfile>)
            : NetworksSignOutUseCase<UserProfile> = NetworksSignOutUseCase(repository)

    @Singleton
    @Provides
    fun provideAuthResponseListenerUseCase(repository: BaseAuthRepository<UserProfile>)
            : AuthResponseListenerUseCase<UserProfile> = AuthResponseListenerUseCase(repository)

    @Singleton
    @Provides
    fun provideSignInWithEmailUseCase(repository: EmailAuthRepository<UserProfile>)
            : SignInWithEmailUseCase<UserProfile> = SignInWithEmailUseCase(repository)

    @Singleton
    @Provides
    fun provideSignInWithSocialNetworkUseCase(repository: SocialNetworkAuthRepository<UserProfile>)
            : SignInWithSocialNetworkUseCase<UserProfile> =
        SignInWithSocialNetworkUseCase(repository)

    @Singleton
    @Provides
    fun provideSignUpUseCase(repository: EmailAuthRepository<UserProfile>)
            : SignUpUseCase<UserProfile> = SignUpUseCase(repository)
}