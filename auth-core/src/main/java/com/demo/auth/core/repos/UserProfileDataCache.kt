package com.demo.auth.core.repos

import androidx.lifecycle.LiveData

/**
 * Created by alexk on 11/29/18.
 * Project android-auth-pack
 */
interface UserProfileDataCache<ProfileDataType> {

    fun getProfileUid(): String?
    fun getProfile(): LiveData<ProfileDataType>
    fun updateProfile(userProfile: ProfileDataType)
    fun deleteProfile()
}