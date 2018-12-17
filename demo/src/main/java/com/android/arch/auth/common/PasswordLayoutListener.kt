package com.android.arch.auth.common

import android.view.View
import com.google.android.material.textfield.TextInputLayout

class PasswordLayoutListener(private val passwordLayout: TextInputLayout) : View.OnFocusChangeListener {

    override fun onFocusChange(v: View?, hasFocus: Boolean): Unit = with(passwordLayout) {
        if (!hasFocus) error = null
        isPasswordVisibilityToggleEnabled = hasFocus
    }
}