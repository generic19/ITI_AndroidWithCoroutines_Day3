package com.basilalasadi.iti.kotlin.productsstateflow.data.source.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.basilalasadi.iti.kotlin.productsstateflow.data.model.FavoritableProduct

@Database(version = 1, entities = [FavoritableProduct::class])
abstract class AppDatabase : RoomDatabase() {
    abstract fun getFavoritableProductDao(): FavoritableProductDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                synchronized(AppDatabase::class) {
                    if (instance == null) {
                        instance = Room.databaseBuilder(
                            context,
                            AppDatabase::class.java,
                            "AppDatabase",
                        ).build()
                    }
                }
            }

            return instance!!
        }
    }
}
