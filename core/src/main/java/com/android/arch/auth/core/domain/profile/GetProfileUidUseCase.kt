package com.android.arch.auth.core.domain.profile

import com.android.arch.auth.core.data.repository.UserProfileDataCache

/**
 * Created by alexk on 9/20/18.
 * Project android-auth-pack
 */
class GetProfileUidUseCase<UserProfileDataType>(private val userProfileDataCache: UserProfileDataCache<UserProfileDataType>) {

    operator fun invoke(): String? = userProfileDataCache.getProfileUid()
}

