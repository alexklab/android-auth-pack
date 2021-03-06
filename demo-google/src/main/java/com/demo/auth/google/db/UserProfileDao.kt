package com.demo.auth.google.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.demo.auth.google.db.UserProfile.Companion.PROFILE_KEY
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

    @Query("SELECT id FROM userProfile WHERE profileKey = $PROFILE_KEY")
    abstract fun getProfileUid(): String?

    @Query("DELETE FROM userProfile WHERE profileKey = $PROFILE_KEY")
    abstract fun deleteProfile()
}