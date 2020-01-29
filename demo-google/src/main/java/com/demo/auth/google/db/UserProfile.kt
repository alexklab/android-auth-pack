package com.demo.auth.google.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.arch.auth.core.data.entity.AuthUserProfile
import java.io.Serializable
import java.util.*

@Entity(tableName = "userProfile")
data class UserProfile(
    val id: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val givenName: String? = null,
    val familyName: String? = null,
    val photoUrl: String? = null,

    @PrimaryKey
    val profileKey: Int = PROFILE_KEY,
    var lastUpdateTimestampMs: Long = Calendar.getInstance().timeInMillis

) : Serializable {

    constructor(profile: AuthUserProfile) : this(
        id = profile.id,
        email = profile.email,
        displayName = profile.name,
        givenName = profile.firstName,
        familyName = profile.lastName,
        photoUrl = profile.picture
    )

    companion object {
        const val PROFILE_KEY: Int = 10500
    }
}