package com.demo.auth.firebase.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.demo.auth.firebase.R
import com.demo.auth.firebase.common.loadIcon
import com.demo.auth.firebase.db.entity.UserInfo
import kotlinx.android.synthetic.main.list_item_user_info.view.*

class UserInfoViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent.let {
    LayoutInflater.from(it.context).inflate(R.layout.list_item_user_info, it, false)
}) {

    fun onBind(info: UserInfo) = with(itemView) {
        uidTextView.text = info.uid
        providerTextView.text = info.providerId
        emailTextView.text = info.email ?: DEFAULT_VALUE
        phoneTextView.text = info.phoneNumber ?: DEFAULT_VALUE
        nameTextView.text = info.displayName ?: DEFAULT_VALUE
        verifyEmailTextView.text = info.isEmailVerified.toString()
        loadIcon(info.photoUrl, iconImageView)
    }

    companion object {
        private const val DEFAULT_VALUE = "-NULL-"
    }

}