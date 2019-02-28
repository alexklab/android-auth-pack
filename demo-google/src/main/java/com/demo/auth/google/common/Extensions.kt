package com.demo.auth.google.common

import android.content.Context
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.android.arch.auth.core.data.entity.AuthResponseErrorType
import com.demo.auth.google.GlideApp
import com.demo.auth.google.R

fun FragmentManager.applyTransaction(transaction: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction()
        .transaction()
        .commit()
}

fun Fragment.applyContext(action: Context.() -> Unit) {
    context?.action() ?: Log.w("applyContext:", "Unsupported operation. context = null")
}

fun Fragment.showFailRequestAlert(errorType: AuthResponseErrorType?, onRetry: () -> Unit) = applyContext {
    Log.w("showFailRequestAlert:", "Error: $errorType")
    AlertDialog.Builder(this)
        .setTitle(R.string.error_response_title)
        .setMessage(R.string.error_response_message)
        .setPositiveButton(R.string.retry) { _, _ -> onRetry() }
        .setNegativeButton(R.string.back) { dialog, _ -> dialog.dismiss() }
        .setCancelable(true)
        .create()
        .apply { setCanceledOnTouchOutside(true) }
        .show()
}

fun Fragment.loadIcon(url: String?, view: ImageView?) {
    view?.let {
        GlideApp.with(this)
            .load(url)
            .into(view)
    }
}