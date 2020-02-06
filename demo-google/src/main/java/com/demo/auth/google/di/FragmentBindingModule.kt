package com.demo.auth.google.di

import androidx.lifecycle.ViewModel
import com.demo.auth.google.ui.SignInFragment
import com.demo.auth.google.ui.SignInViewModel
import com.demo.auth.google.ui.UserProfileFragment
import com.demo.auth.google.ui.UserProfileViewModel
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
     * Generates an [AndroidInjector] for the [UserProfileFragment].
     */
    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun contributeUserProfileFragment(): UserProfileFragment

    /**
     * The ViewModels are created by Dagger in a map. Via the @ViewModelKey, we define that we
     * want to get a [SignInViewModel] class.
     */
    @Binds
    @IntoMap
    @ViewModelKey(SignInViewModel::class)
    internal abstract fun bindSignInViewModel(viewModel: SignInViewModel): ViewModel

    /**
     * The ViewModels are created by Dagger in a map. Via the @ViewModelKey, we define that we
     * want to get a [UserProfileViewModel] class.
     */
    @Binds
    @IntoMap
    @ViewModelKey(UserProfileViewModel::class)
    internal abstract fun bindUserProfileViewModel(viewModel: UserProfileViewModel): ViewModel
}

