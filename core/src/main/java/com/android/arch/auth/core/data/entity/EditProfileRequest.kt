package com.android.arch.auth.core.data.entity

import android.net.Uri

class EditProfileRequest(
    val emailParam: RequestParam<String> = RequestParam(),
    val loginParam: RequestParam<String> = RequestParam(),
    val photoUriParam: RequestParam<Uri> = RequestParam()
) {
    fun resetAll() {
        emailParam.reset()
        loginParam.reset()
        photoUriParam.reset()
    }

    fun editEmail(valueFrom: String?, valueTo: String?): EditProfileRequest {
        emailParam.edit(valueFrom, valueTo)
        return this
    }

    fun editLogin(valueFrom: String?, valueTo: String?): EditProfileRequest {
        loginParam.edit(valueFrom, valueTo)
        return this
    }

    fun editPhotoUri(valueFrom: Uri?, valueTo: Uri?): EditProfileRequest {
        photoUriParam.edit(valueFrom, valueTo)
        return this
    }

    fun isEmpty(): Boolean {
        return emailParam.isChanged || loginParam.isChanged || photoUriParam.isChanged
    }
}