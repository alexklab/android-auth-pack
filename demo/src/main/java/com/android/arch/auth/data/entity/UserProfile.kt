package com.android.arch.auth.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

/**
 * Created by alexk on 12/17/18.
 * Project android-auth-pack
 */

@Entity(tableName = "userProfile")
data class UserProfile(
    val uid: String? = null,
    val name: String? = null,
    val email: String? = null,
    val logoUrl: String? = null,
    val networkProvider: String? = null,

    @PrimaryKey
    val profileKey: Int = PROFILE_KEY,
    var lastUpdateTimestampMs: Long = Calendar.getInstance().timeInMillis

) : Serializable {

    companion object {
        const val PROFILE_KEY: Int = 10500
    }
}