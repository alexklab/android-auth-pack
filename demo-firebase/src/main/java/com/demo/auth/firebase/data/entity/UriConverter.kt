package com.demo.auth.firebase.data.entity

import android.net.Uri
import androidx.room.TypeConverter

class UriConverter {

    @TypeConverter
    fun fromTimestamp(data: String?): Uri? = data?.let { Uri.parse(it) }

    @TypeConverter
    fun someObjectListToString(someObject: Uri?): String? = someObject?.toString()
}