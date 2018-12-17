package com.android.arch.auth.core.testutils

import com.android.arch.auth.core.common.CoroutineContextProvider
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Rule to be used in tests that sets a coroutines execute at the same thread.
 */
class CoroutineContextProviderRule : TestWatcher() {

    override fun starting(description: Description?) {
        super.starting(description)
        CoroutineContextProvider.resetContext(isUnconfinedMode = true)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        CoroutineContextProvider.resetContext(isUnconfinedMode = false)
    }
}