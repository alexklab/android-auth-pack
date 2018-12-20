package com.demo.auth.firebase.data.database

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.OnLifecycleEvent
import com.demo.auth.core.common.CoroutineContextProvider
import com.demo.auth.core.repos.UserProfileDataCache
import com.demo.auth.firebase.data.entity.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Created by alexk on 12/17/18.
 * Project android-auth-pack
 */
class DatabaseProvider : UserProfileDataCache<UserProfile>, LifecycleObserver {

    /**
     * Should be called on Activity.onCreate
     */
    fun onCreate(activity: ComponentActivity) {
        if (db?.isOpen != true) {
            db = UserProfileDb.create(activity)
        } else {
            Log.w("openDb:", "Db is actually open")
        }

        activity.lifecycle.apply {
            removeObserver(this@DatabaseProvider)
            addObserver(this@DatabaseProvider)
        }
    }

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        dbJob.cancel()
        db?.close()
        db = null
    }

    override fun getProfileUid(): String? =
        syncDbOperation { dao().getProfileUid() }

    override fun getProfile(): LiveData<UserProfile> =
        syncDbOperation { dao().getProfile() }

    override fun updateProfile(userProfile: UserProfile) {
        asyncDbOperation { dao().updateProfile(userProfile) }
    }

    override fun deleteProfile() {
        asyncDbOperation { dao().deleteProfile() }
    }

    private fun <Response> syncDbOperation(operation: UserProfileDb.() -> Response): Response =
        db?.operation() ?: throw IllegalStateException("db not open")

    private fun asyncDbOperation(operation: UserProfileDb.() -> Unit) {
        ioScope.launch { syncDbOperation(operation) }
    }

    /**
     * This is the job for all coroutines started by db.
     *
     * Cancelling this job will cancel all coroutines started by this db.
     */
    private val dbJob = Job()
    /**
     * This is the main scope for all coroutines launched by db.
     *
     * Since we pass dbJob, you can cancel all coroutines launched by ioScope by calling
     * dbJob.cancel()
     */
    private val ioScope = CoroutineScope(CoroutineContextProvider.IO + dbJob)

    private var db: UserProfileDb? = null
}