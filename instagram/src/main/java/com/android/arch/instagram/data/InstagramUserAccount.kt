package com.android.arch.instagram.data

import com.google.gson.annotations.SerializedName

/**
 * Created by alexk on 12/26/18.
 * Project android-auth-pack
 */
data class InstagramUserAccount(
    @SerializedName("id") val id: String,
    @SerializedName("username") val userName: String,
    @SerializedName("profile_picture") val profilePicture: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("bio") val bio: String,
    @SerializedName("website") val website: String,
    @SerializedName("is_business") val isBusiness: Boolean,
    @SerializedName("counts") val counts: UserInfoCounts
)