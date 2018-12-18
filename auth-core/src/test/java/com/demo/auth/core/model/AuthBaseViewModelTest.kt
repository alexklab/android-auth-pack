package com.demo.auth.core.model

import com.demo.auth.core.entity.AuthResponse
import com.demo.auth.core.testutils.LiveDataTest

/**
 * Created by alexk on 12/13/18.
 * Project android-auth-pack
 */
abstract class AuthBaseViewModelTest<UserProfileDataType, ModelType : AuthBaseViewModel<UserProfileDataType>> : LiveDataTest<ModelType> {

    fun responseTestCase(setup: () -> Unit = {},
                         action: ModelType.() -> Unit,
                         expected: (AuthResponse<UserProfileDataType>?) -> Unit) = awaitTestCase(
            setup = setup,
            action = action,
            liveData = AuthBaseViewModel<UserProfileDataType>::response,
            expected = { expected(it?.getContentIfNotHandled()) }
    )

}