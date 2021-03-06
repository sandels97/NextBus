package com.santtuhyvarinen.nextbus.models

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.santtuhyvarinen.nextbus.R
import org.joda.time.DateTime

//Model that holds information about a bus stop
class BusStopModel (var route : String, var stopName : String, var destinationName : String, var routeType : Int, var distance : Int, val stopTimes : List<DateTime>) {

    var isFavorite = false

    companion object {
        //Based on https://developers.google.com/transit/gtfs/reference
        const val ROUTE_TYPE_TRAM = 0
        const val ROUTE_TYPE_SUBWAY = 1
        const val ROUTE_TYPE_RAIL = 2
        const val ROUTE_TYPE_BUS = 3
        const val ROUTE_TYPE_UNKNOWN = -1
    }
    fun getDrawable(context: Context) : Drawable {
        when(routeType) {
            ROUTE_TYPE_TRAM -> return ContextCompat.getDrawable(context, R.drawable.ic_tram)!!
            ROUTE_TYPE_SUBWAY -> return ContextCompat.getDrawable(context, R.drawable.ic_subway)!!
            ROUTE_TYPE_RAIL, 109 -> return ContextCompat.getDrawable(context, R.drawable.ic_train)!!
            ROUTE_TYPE_BUS -> return ContextCompat.getDrawable(context, R.drawable.ic_bus)!!
            else -> {
                Log.d("route", "Unknown transport type: " + routeType.toString())
                return ContextCompat.getDrawable(context, R.drawable.ic_transport_other)!!
            }
        }
    }

    override fun toString(): String {
        return "$route, $stopName, $routeType, $distance"
    }
}

