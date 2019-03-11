package com.demo.auth.firebase.common

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.android.arch.auth.core.data.entity.AuthError
import com.demo.auth.firebase.GlideApp
import com.demo.auth.firebase.R
import com.google.android.material.textfield.TextInputLayout

/**
 * Created by alexk on 12/19/18.
 * Project android-auth-pack
 */

fun EditText?.textValue(): String = this?.text?.toString().orEmpty()

fun clearAllErrors(vararg layouts: TextInputLayout?) {
    layouts.forEach { it?.error = null }
}

fun Fragment.applyContext(action: Context.() -> Unit) {
    context?.action() ?: Log.w("applyContext:", "Unsupported operation. context = null")
}

fun Fragment.showFailRequestAlert(error: AuthError?, onRetry: () -> Unit) = applyContext {
    Log.w("showFailRequestAlert:", "Error: $error", error?.exception)
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

fun FragmentManager.applyTransaction(transaction: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction()
        .transaction()
        .commit()
}

fun Fragment.loadIcon(url: Uri?, view: ImageView?) {
    view?.let {
        GlideApp.with(this)
            .load(url)
            .into(view)
    }
}

fun RecyclerView.ViewHolder.loadIcon(url: String?, view: ImageView?) {
    view?.let {
        GlideApp.with(this.itemView.context)
            .load(url)
            .into(view)
    }
}