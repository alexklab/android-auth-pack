package com.demo.auth.core.domain.profile

import com.demo.auth.core.repos.UserProfileDataCache

/**
 * Created by alexk on 9/20/18.
 * Project android-auth-pack
 */
class UpdateProfileUseCase<UserProfileDataType>(private val userProfileDataCache: UserProfileDataCache<UserProfileDataType>) {

    operator fun invoke(userProfile: UserProfileDataType): Unit =
            userProfileDataCache.updateProfile(userProfile)
}
