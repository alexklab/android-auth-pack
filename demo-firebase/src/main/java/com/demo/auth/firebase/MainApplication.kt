package com.demo.auth.firebase

import android.content.Context
import androidx.multidex.MultiDex
import com.demo.auth.firebase.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import timber.log.Timber

/**
 * Created by alexk on 12/17/18.
 * Project android-auth-pack
 */
class MainApplication : DaggerApplication() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.factory().create(this)
    }

}