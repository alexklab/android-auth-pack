package com.demo.auth.firebase.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.demo.auth.firebase.data.entity.UserInfo

class UserInfoAdapter : RecyclerView.Adapter<UserInfoViewHolder>() {

    private val data = mutableListOf<UserInfo>()

    fun updateAll(usersInfo: List<UserInfo>) {
        data.clear()
        data.addAll(usersInfo)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        UserInfoViewHolder(parent)

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: UserInfoViewHolder, position: Int) {
        holder.onBind(data[position])
    }

}