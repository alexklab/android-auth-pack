package com.android.arch.instagram.data

import com.google.gson.annotations.SerializedName

/**
 * Created by alexk on 12/28/18.
 * Project android-auth-pack
 */
data class UserInfoResponse(
    @SerializedName("meta") val meta: MetaResponse?,
    @SerializedName("data") val account: InstagramUserAccount?
)