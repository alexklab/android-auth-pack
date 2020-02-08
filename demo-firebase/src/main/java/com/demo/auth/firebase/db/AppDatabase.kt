package com.demo.auth.firebase.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.demo.auth.firebase.db.entity.UriConverter
import com.demo.auth.firebase.db.entity.UserInfo
import com.demo.auth.firebase.db.entity.UserInfoConverter
import com.demo.auth.firebase.db.entity.UserProfile

/**
 * Created by alexk on 12/17/18.
 * Project android-auth-pack
 */
@Database(entities = [UserProfile::class, UserInfo::class], version = 1, exportSchema = false)
@TypeConverters(UriConverter::class, UserInfoConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dao(): UserProfileDao

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()

        private const val DB_NAME = "userProfile.db"
    }
}