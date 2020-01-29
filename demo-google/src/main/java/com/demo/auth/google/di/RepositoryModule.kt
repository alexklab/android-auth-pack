package com.demo.auth.google.di


import com.android.arch.auth.core.data.repository.SocialNetworkAuthRepository
import com.android.arch.auth.core.data.repository.UserProfileDataCache
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

@Module
class RepositoryModule {

    @Provides
    fun provideUserProfileDataCache(database: AppDatabase):
            UserProfileDataCache<UserProfile> = UserProfileRepository(database.dao())

    @Provides
    fun provideSocialNetworkAuthRepository(repository: AuthRepository):
            SocialNetworkAuthRepository<UserProfile> = repository

    /**
     * Provider for view model delegate
     */
    @Provides
    fun provideSignInViewModel(
        repository: SocialNetworkAuthRepository<UserProfile>,
        dataCache: UserProfileDataCache<UserProfile>
    ): SignInWithSocialNetworksViewModel<UserProfile> = SignInWithSocialNetworksViewModel(
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
    fun provideNetworksSignOutUseCase(repository:SocialNetworkAuthRepository<UserProfile>)
            : NetworksSignOutUseCase<UserProfile> = NetworksSignOutUseCase(repository)
}