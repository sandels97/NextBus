package com.santtuhyvarinen.nextbus.handlers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.PointF
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class LocationHandler(private val context: Context, private val locationHandlerListener: LocationHandlerListener) {

    //x = latitude, y = longitude
    var location : PointF? = null

    lateinit var fusedLocationProviderClient : FusedLocationProviderClient

    init {
        initLocationManager()
    }

    fun initLocationManager() {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOCATION_TAG, "No permission to get location")
            return
        }

        val locationRequest = LocationRequest()

        //Update every 10 seconds if location has moved 10 meters or more
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 10000

        locationRequest.smallestDisplacement = 10f

        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                super.onLocationResult(result)

                if(result != null) {
                    Log.d(LOCATION_TAG, "Updated location")
                    location = PointF(result.lastLocation.latitude.toFloat(), result.lastLocation.longitude.toFloat())
                    locationHandlerListener.locationUpdated()
                }
            }
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        val result = fusedLocationProviderClient.lastLocation
        result.addOnSuccessListener {
            if(it != null) {
                Log.d(LOCATION_TAG, "Updated location with last known location")
                location = PointF(it.latitude.toFloat(), it.longitude.toFloat())
                locationHandlerListener.locationUpdated()
            } else {
                Log.d(LOCATION_TAG, "Could not find last known location")
                locationHandlerListener.locationOff()
            }
        }.addOnFailureListener {
            Log.d(LOCATION_TAG, "Could not find last known location")
            locationHandlerListener.locationOff()
        }
    }

    companion object {
        const val LOCATION_TAG = "location_tag"
    }

    interface LocationHandlerListener {
        fun locationUpdated()
        fun locationOff()
    }
}