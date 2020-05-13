package com.santtuhyvarinen.nextbus.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.santtuhyvarinen.nextbus.models.FavoriteModel

@Database(entities = arrayOf(FavoriteModel::class), version = 1)
abstract class FavoritesDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    companion object {
        private var INSTANCE : FavoritesDatabase? = null
        const val DATABASE_TAG = "database_tag"
        fun getInstance(context: Context) : FavoritesDatabase? {
            if(INSTANCE == null) {
                synchronized(FavoritesDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext, FavoritesDatabase::class.java, "favorites.db").build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}