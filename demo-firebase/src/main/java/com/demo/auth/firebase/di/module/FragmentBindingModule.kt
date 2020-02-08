package com.demo.auth.firebase.di.module

import androidx.lifecycle.ViewModel
import com.demo.auth.firebase.di.FragmentScoped
import com.demo.auth.firebase.di.ViewModelKey
import com.demo.auth.firebase.ui.*
import com.demo.auth.firebase.ui.signin.SignInFragment
import com.demo.auth.firebase.ui.signin.SignInWithEmailViewModel
import com.demo.auth.firebase.ui.signin.SignInWithSocialNetworksViewModel
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerFragment
import dagger.multibindings.IntoMap


/**
 * Module where classes needed to create the [DaggerFragment] are defined.
 */
@Module
@Suppress("UNUSED")
abstract class FragmentBindingModule {

    /**
     * Generates an [AndroidInjector] for the [SignInFragment].
     */
    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun contributeSignInFragment(): SignInFragment

    /**
     * Generates an [AndroidInjector] for the [SignUpFragment].
     */
    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun contributeSignUpFragment(): SignUpFragment

    /**
     * Generates an [AndroidInjector] for the [ChangePasswordFragment].
     */
    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun contributeChangePasswordFragment(): ChangePasswordFragment

    /**
     * Generates an [AndroidInjector] for the [RecoveryPasswordFragment].
     */
    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun contributeRecoveryPasswordFragment(): RecoveryPasswordFragment

    /**
     * Generates an [AndroidInjector] for the [UserProfileFragment].
     */
    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun contributeUserProfileFragment(): UserProfileFragment

    /**
     * The ViewModels are created by Dagger in a map. Via the @ViewModelKey, we define that we
     * want to get a [SignInWithEmailViewModel] class.
     */
    @Binds
    @IntoMap
    @ViewModelKey(SignInWithEmailViewModel::class)
    internal abstract fun bindSignInWithEmailViewModel(viewModel: SignInWithEmailViewModel): ViewModel

    /**
     * The ViewModels are created by Dagger in a map. Via the @ViewModelKey, we define that we
     * want to get a [SignInWithSocialNetworksViewModel] class.
     */
    @Binds
    @IntoMap
    @ViewModelKey(SignInWithSocialNetworksViewModel::class)
    internal abstract fun bindSignInWithSocialNetworksViewModel(viewModel: SignInWithSocialNetworksViewModel): ViewModel


    /**
     * The ViewModels are created by Dagger in a map. Via the @ViewModelKey, we define that we
     * want to get a [UserProfileViewModel] class.
     */
    @Binds
    @IntoMap
    @ViewModelKey(UserProfileViewModel::class)
    internal abstract fun bindUserProfileViewModel(viewModel: UserProfileViewModel): ViewModel
}

