package com.android.arch.auth.core.common.extensions

import com.android.arch.auth.core.common.CoroutineContextProvider
import kotlinx.coroutines.withContext

/**
 * Created by alexk on 11/15/18.
 * Project android-auth-pack
 */
suspend fun withIOContext(func: () -> Unit) =
        withContext(CoroutineContextProvider.IO) { func() }