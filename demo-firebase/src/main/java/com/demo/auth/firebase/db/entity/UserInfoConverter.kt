package com.demo.auth.firebase.db.entity

import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.Gson

class UserInfoConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(data: String?): List<UserInfo> =
        try {
            if (data.isNullOrEmpty())
                emptyList()
            else
                gson.fromJson(data, Array<UserInfo>::class.java).toList()
        } catch (e: Exception) {
            Log.w("UserInfoConverter", "Fail parse UserInfo from json: ${e.message}", e)
            emptyList()
        }

    @TypeConverter
    fun someObjectListToString(someObjects: List<UserInfo>?): String? =
        try {
            gson.toJson(someObjects)
        } catch (e: Exception) {
            Log.w("Fail UserInfo to json: ${e.message}", e)
            ""
        }
}