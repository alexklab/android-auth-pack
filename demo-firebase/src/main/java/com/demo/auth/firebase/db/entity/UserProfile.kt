package com.demo.auth.firebase.db.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import java.io.Serializable
import java.util.*

/**
 * Created by alexk on 12/17/18.
 * Project android-auth-pack
 */

@Entity(tableName = "userProfile")
data class UserProfile(
    val uid: String? = null,
    val providerId: String? = null,
    val email: String? = null,

    @TypeConverters(UriConverter::class)
    val photoUrl: Uri? = null,

    val displayName: String? = null,
    val phoneNumber: String? = null,
    val isEmailVerified: Boolean = false,

    val creationTimestamp: Long? = null,
    val lastSignInTimestamp: Long? = null,

    @TypeConverters(UserInfoConverter::class)
    val providersData: List<UserInfo> = emptyList(),
    val isAnonymous: Boolean = false,

    @PrimaryKey
    val profileKey: Int = PROFILE_KEY,
    var lastUpdateTimestampMs: Long = Calendar.getInstance().timeInMillis

) : Serializable {

    constructor(user: FirebaseUser) : this(
        uid = user.uid,
        providerId = user.providerId,
        email = user.email,
        photoUrl = user.photoUrl,
        displayName = user.displayName,
        phoneNumber = user.phoneNumber,
        creationTimestamp = user.metadata?.creationTimestamp,
        lastSignInTimestamp = user.metadata?.lastSignInTimestamp,
        providersData = user.providerData.mapNotNull { UserInfo(it) },
        isAnonymous = user.isAnonymous,
        isEmailVerified = user.isEmailVerified
    )

    fun containsEmailAuthProvider(): Boolean =
        providersData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }

    companion object {
        const val PROFILE_KEY: Int = 10500
    }
}