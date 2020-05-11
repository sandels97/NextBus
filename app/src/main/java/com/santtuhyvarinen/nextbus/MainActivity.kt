package com.santtuhyvarinen.nextbus

import android.Manifest
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.santtuhyvarinen.nextbus.utils.PermissionUtil

class MainActivity : AppCompatActivity() {

    //All the permissions that app requires
    val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE
    )

    //Request code for permissions
    val PERMISSION_ALL = 123

    var permissionsGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Navigation
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //Permission - Ask user for permissions if they are not granted
        if(!PermissionUtil.hasPermissions(this, *PERMISSIONS)) {
            Log.d(PermissionUtil.PERMISSION_TAG, "Permissions not granted - Ask for permissions")
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            Log.d(PermissionUtil.PERMISSION_TAG, "All permissions granted")
            permissionsGranted = true
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissionsGranted = PermissionUtil.hasPermissions(this, *PERMISSIONS)
        Log.d(PermissionUtil.PERMISSION_TAG, "Permissions granted? " + permissionsGranted)
    }
}
