package com.android.arch.auth.core.model

import androidx.lifecycle.ViewModel
import com.android.arch.auth.core.common.CoroutineContextProvider
import com.android.arch.auth.core.common.extensions.withIOContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    /**
     * Cancel all coroutines when the ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    /**
     * This is the job for all coroutines started by this ViewModel.
     *
     * Cancelling this job will cancel all coroutines started by this ViewModel.
     */
    private val viewModelJob = Job()
    /**
     * This is the main scope for all coroutines launched by MainViewModel.
     *
     * Since we pass viewModelJob, you can cancel all coroutines launched by uiScope by calling
     * viewModelJob.cancel()
     */
    protected val uiScope = CoroutineScope(CoroutineContextProvider.Main + viewModelJob)

    protected fun launchAsync(block: () -> Unit) {
        uiScope.launch {
            withIOContext(block)
        }
    }
}