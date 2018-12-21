package com.android.arch.auth.core.testutils

import androidx.lifecycle.LiveData

interface LiveDataTest<InstanceType> {

    val instance: InstanceType

    fun <ResultType : Any> awaitTestCase(
            setup: () -> Unit = {},
            liveData: InstanceType.() -> LiveData<ResultType>,
            action: InstanceType.() -> Unit,
            expected: (ResultType?) -> Unit): Unit = with(instance) {
        setup()
        action()
        expected(LiveDataUtil.awaitValue(liveData()))
    }

    fun <ResultType : Any> testCase(
            setup: () -> Unit = {},
            action: InstanceType.() -> ResultType?,
            expected: (ResultType?) -> Unit) {
        setup()
        expected(instance.action())
    }
}