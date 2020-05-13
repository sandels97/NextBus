package com.santtuhyvarinen.nextbus.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "favoriteRoutes")
data class FavoriteModel(
    @PrimaryKey @ColumnInfo(name = "route")var route : String) : Parcelable