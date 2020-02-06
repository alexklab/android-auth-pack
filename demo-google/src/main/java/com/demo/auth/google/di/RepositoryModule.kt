package com.demo.auth.google.di


import com.android.arch.auth.core.data.repository.SocialNetworkAuthRepository
import com.android.arch.auth.core.data.repository.UserProfileDataCache
import com.android.arch.auth.core.domain.auth.AuthResponseListenerUseCase
import com.android.arch.auth.core.domain.auth.NetworksSignOutUseCase
import com.android.arch.auth.core.domain.auth.SignInWithSocialNetworkUseCase
import com.android.arch.auth.core.domain.profile.DeleteProfileUseCase
import com.android.arch.auth.core.domain.profile.GetProfileUseCase
import com.android.arch.auth.core.domain.profile.UpdateProfileUseCase
import com.android.arch.auth.core.model.SignInWithSocialNetworksViewModel
import com.demo.auth.google.db.AppDatabase
import com.demo.auth.google.db.UserProfile
import com.demo.auth.google.repo.AuthRepository
import com.demo.auth.google.repo.UserProfileRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepositoryModule {

    @Singleton
    @Provides
    fun provideUserProfileDataCache(database: AppDatabase):
            UserProfileDataCache<UserProfile> {
        return UserProfileRepository(database.dao())
    }

    @Singleton
    @Provides
    fun provideSocialNetworkAuthRepository(repository: AuthRepository): SocialNetworkAuthRepository<UserProfile> {
        return repository
    }

    /**
     * Provider for view model delegate
     */
    @Provides
    fun provideSignInWithSocialNetworksViewModel(
        listenerUseCase: AuthResponseListenerUseCase<UserProfile>,
        repository: SocialNetworkAuthRepository<UserProfile>,
        dataCache: UserProfileDataCache<UserProfile>
    ): SignInWithSocialNetworksViewModel<UserProfile> = SignInWithSocialNetworksViewModel(
        listenerUseCase,
        SignInWithSocialNetworkUseCase(repository),
        UpdateProfileUseCase(dataCache)
    )

    // UseCases

    @Provides
    fun provideGetProfileUseCase(dataCache: UserProfileDataCache<UserProfile>):
            GetProfileUseCase<UserProfile> = GetProfileUseCase(dataCache)

    @Provides
    fun provideDeleteProfileUseCase(dataCache: UserProfileDataCache<UserProfile>):
            DeleteProfileUseCase<UserProfile> = DeleteProfileUseCase(dataCache)

    @Provides
    fun provideNetworksSignOutUseCase(repository: SocialNetworkAuthRepository<UserProfile>)
            : NetworksSignOutUseCase<UserProfile> = NetworksSignOutUseCase(repository)

    @Provides
    fun provideAuthResponseListenerUseCase(repository: SocialNetworkAuthRepository<UserProfile>)
            : AuthResponseListenerUseCase<UserProfile> = AuthResponseListenerUseCase(repository)
}