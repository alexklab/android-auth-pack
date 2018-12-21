package com.android.arch.auth.core.data.network

import java.lang.Exception

typealias NetworkSignInCallBack<DataType> = (DataType?, ParamsBundle?, Exception?) -> Unit