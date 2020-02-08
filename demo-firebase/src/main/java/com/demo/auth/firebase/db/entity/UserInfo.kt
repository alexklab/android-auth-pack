package com.demo.auth.firebase.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.auth.UserInfo

@Entity
data class UserInfo(
    @PrimaryKey
    val uid: String, // Returns a user identifier as specified by the authentication provider.
    val providerId: String, // Returns the unique identifier of the provider type that this instance corresponds to.
    val email: String?,// Returns the email address corresponding to the user's account in the specified provider, if available.
    val photoUrl: String?, // Returns a Uri to the user's profile picture, if available.
    val displayName: String?, // Returns the user's display name, if available.
    val phoneNumber: String?, // Returns the phone number corresponding to the user's account, if available, or null if none exists.
    val isEmailVerified: Boolean // Returns true if the user's email is verified.

) {
    constructor(info: UserInfo) : this(
        uid = info.uid,
        providerId = info.providerId,
        email = info.email,
        photoUrl = info.photoUrl?.toString(),
        displayName = info.displayName,
        phoneNumber = info.phoneNumber,
        isEmailVerified = info.isEmailVerified
    )
}