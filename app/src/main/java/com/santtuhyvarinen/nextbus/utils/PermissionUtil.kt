package com.santtuhyvarinen.nextbus.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

//Handles permissions - For now just the location permission
class PermissionUtil {
    companion object {
        //For logging
        const val PERMISSION_TAG = "permission_tag"

        fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}