package com.demo.auth.firebase.common

import android.view.View
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputLayout.END_ICON_NONE
import com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE

class PasswordLayoutListener(private val passwordLayout: TextInputLayout) :
    View.OnFocusChangeListener {

    override fun onFocusChange(v: View?, hasFocus: Boolean): Unit = with(passwordLayout) {
        if (!hasFocus) error = null
        endIconMode = if (hasFocus) END_ICON_PASSWORD_TOGGLE else END_ICON_NONE
    }
}