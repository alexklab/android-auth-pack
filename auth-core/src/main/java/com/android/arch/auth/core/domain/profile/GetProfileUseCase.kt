package com.android.arch.auth.core.domain.profile

import androidx.lifecycle.LiveData
import com.android.arch.auth.core.data.repository.UserProfileDataCache

/**
 * Created by alexk on 9/20/18.
 * Project android-auth-pack
 */
class GetProfileUseCase<UserProfileDataType>(private val userProfileDataCache: UserProfileDataCache<UserProfileDataType>) {

    operator fun invoke(): LiveData<UserProfileDataType> = userProfileDataCache.getProfile()
}

