package com.demo.auth.google.repo

import androidx.lifecycle.LiveData
import com.android.arch.auth.core.data.repository.UserProfileDataCache
import com.demo.auth.google.db.UserProfile
import com.demo.auth.google.db.UserProfileDao

class UserProfileRepository(private val dao: UserProfileDao) : UserProfileDataCache<UserProfile> {

    override fun getProfileUid(): String? = dao.getProfileUid()

    override fun getProfile(): LiveData<UserProfile> = dao.getProfile()

    override fun updateProfile(userProfile: UserProfile) = dao.updateProfile(userProfile)

    override fun deleteProfile() = dao.deleteProfile()

}