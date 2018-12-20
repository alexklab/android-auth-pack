package com.demo.auth.firebase.data.network

import java.lang.Exception

typealias NetworkSignInCallBack<DataType> = (DataType?, Exception?) -> Unit