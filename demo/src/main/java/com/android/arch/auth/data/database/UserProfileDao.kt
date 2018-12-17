package com.android.arch.auth.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.arch.auth.data.entity.UserProfile
import com.android.arch.auth.data.entity.UserProfile.Companion.PROFILE_KEY
import java.util.*

@Dao
abstract class UserProfileDao {

    @Transaction
    open fun updateProfile(userProfile: UserProfile) {
        userProfile.lastUpdateTimestampMs = Calendar.getInstance().timeInMillis
        insert(userProfile)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insert(userProfile: UserProfile)

    @Query("SELECT * FROM userProfile WHERE profileKey = $PROFILE_KEY")
    abstract fun getProfile(): LiveData<UserProfile>

    @Query("SELECT uid FROM userProfile WHERE profileKey = $PROFILE_KEY")
    abstract fun getProfileUid(): String?

    @Query("DELETE FROM userProfile WHERE profileKey = $PROFILE_KEY")
    abstract fun deleteProfile()
}