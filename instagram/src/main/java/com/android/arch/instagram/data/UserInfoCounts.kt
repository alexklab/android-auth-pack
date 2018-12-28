package com.android.arch.instagram.data

import com.google.gson.annotations.SerializedName

data class UserInfoCounts(
    @SerializedName("media") val media: Int,
    @SerializedName("follows") val follows: Int,
    @SerializedName("followed_by") val followedBy: Int
)