package com.demo.auth.google.di

import com.demo.auth.google.ui.SignInFragment
import com.demo.auth.google.ui.UserProfileFragment
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerFragment


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
}

