package com.santtuhyvarinen.nextbus

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.graphics.PointF
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
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

        //Update every 10 seconds
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 10000

        //Set the smallest displacement when should update the location
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val updateDisplacement = sharedPreferences.getInt("update_displacement_key", 50)
        locationRequest.smallestDisplacement = updateDisplacement.toFloat()

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
            Log.d(LOCATION_TAG, "Updated location with last known location")
            location = PointF(it.latitude.toFloat(), it.longitude.toFloat())
            locationHandlerListener.locationUpdated()
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