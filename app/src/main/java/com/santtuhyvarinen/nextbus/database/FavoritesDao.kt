package com.santtuhyvarinen.nextbus.database

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.santtuhyvarinen.nextbus.models.FavoriteModel

//Handles interaction with the database
@Dao
interface FavoritesDao {
    @Query("SELECT * from favoriteRoutes")
    fun getAll() : List<FavoriteModel>

    @Insert(onConflict = REPLACE)
    fun insert(favoriteModel: FavoriteModel)

    @Delete
    fun delete(favoriteModel: FavoriteModel)
}