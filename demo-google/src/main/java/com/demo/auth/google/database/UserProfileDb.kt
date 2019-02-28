package com.demo.auth.google.database

import android.content.Context
import androidx.room.*
import com.demo.auth.google.entity.UserProfile

/**
 * Created by alexk on 12/17/18.
 * Project android-auth-pack
 */
@Database(entities = [UserProfile::class], version = 1, exportSchema = false)
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