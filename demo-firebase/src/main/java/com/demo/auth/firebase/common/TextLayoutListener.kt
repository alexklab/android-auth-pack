package com.demo.auth.firebase.common

import android.view.View
import com.google.android.material.textfield.TextInputLayout

/**
 * Created by alexk on 12/17/18.
 * Project android-auth-pack
 */
class TextLayoutListener(private val passwordLayout: TextInputLayout) : View.OnFocusChangeListener {

    override fun onFocusChange(v: View?, hasFocus: Boolean): Unit = with(passwordLayout) {
        if (!hasFocus) error = null
    }
}