package com.demo.auth.firebase.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.demo.auth.firebase.data.entity.UriConverter
import com.demo.auth.firebase.data.entity.UserInfo
import com.demo.auth.firebase.data.entity.UserInfoConverter
import com.demo.auth.firebase.data.entity.UserProfile

/**
 * Created by alexk on 12/17/18.
 * Project android-auth-pack
 */
@Database(entities = [UserProfile::class, UserInfo::class], version = 1, exportSchema = false)
@TypeConverters(UriConverter::class, UserInfoConverter::class)
abstract class UserProfileDb : RoomDatabase() {

    abstract fun dao(): UserProfileDao

    companion object {

        fun create(context: Context): UserProfileDb =
            Room.databaseBuilder(context, UserProfileDb::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()

        private const val DB_NAME = "userProfile.db"
    }
}