package com.demo.auth.firebase.db

import androidx.lifecycle.LiveData
import com.android.arch.auth.core.data.repository.UserProfileDataCache
import com.demo.auth.firebase.db.entity.UserProfile

class UserProfileRepository(private val dao: UserProfileDao) : UserProfileDataCache<UserProfile> {

    override fun getProfileUid(): String? = dao.getProfileUid()

    override fun getProfile(): LiveData<UserProfile> = dao.getProfile()

    override fun updateProfile(userProfile: UserProfile) = dao.updateProfile(userProfile)

    override fun deleteProfile() = dao.deleteProfile()

}