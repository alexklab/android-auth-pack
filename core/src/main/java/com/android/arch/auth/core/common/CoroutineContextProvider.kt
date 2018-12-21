package com.android.arch.auth.core.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.coroutines.CoroutineContext

/**
 * Created by alexk on 11/20/18.
 * Project android-auth-pack
 */
object CoroutineContextProvider {

    val IO: CoroutineContext get() = ioContext
    val Main: CoroutineContext get() = mainContext

    @ExperimentalCoroutinesApi
    fun resetContext(isUnconfinedMode: Boolean) {
        if (isUnconfinedMode) {
            ioContext = Dispatchers.Unconfined
            mainContext = Dispatchers.Unconfined
        } else {
            ioContext = Dispatchers.IO
            mainContext = Dispatchers.Main
        }
    }

    private var ioContext: CoroutineContext = Dispatchers.IO
    private var mainContext: CoroutineContext = Dispatchers.Main
}