package com.ma.development.runnertracker.common

import android.content.Context
import android.location.Location
import android.os.Build
import androidx.fragment.app.Fragment
import com.ma.development.runnertracker.common.Constants.LOCATION_PERMISSION_REQUEST_CODE
import com.ma.development.runnertracker.services.Polyline
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object TrackingUtility {

    fun checkLocationPermissions(context: Context) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.hasPermissions(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }

    fun requestLocationPermissions(host: Fragment) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            EasyPermissions.requestPermissions(
                host,
                "You should accept this permission to be able to use this app",
                LOCATION_PERMISSION_REQUEST_CODE,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        else
            EasyPermissions.requestPermissions(
                host,
                "You should accept this permission to be able to use this app",
                LOCATION_PERMISSION_REQUEST_CODE,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            )
    }

    fun getFormattedStopWatchTime(timeInMillis: Long, millisEnabled: Boolean = false): String {
        var millis = timeInMillis
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        millis -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= TimeUnit.MINUTES.toMillis(minutes)
        val secs = TimeUnit.MILLISECONDS.toSeconds(millis)
        if (!millisEnabled) {
            return "${if (hours < 10) "0" else ""}$hours:" +
                    "${if (minutes < 10) "0" else ""}$minutes:" +
                    "${if (secs < 10) "0" else ""}$secs"
        }
        millis -= TimeUnit.SECONDS.toMillis(secs)
        millis /= 10
        return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (secs < 10) "0" else ""}$secs:" +
                "${if (millis < 10) "0" else ""}$millis"
    }

    fun getFormattedDateFromTimeStamp (dateInMillis : Long) : String{
        val timeStamp : Date
         Calendar.getInstance().apply {
            timeInMillis = dateInMillis
            timeStamp = time
        }
        val dateFormat = SimpleDateFormat("dd/MM/yy",Locale.getDefault())
        return dateFormat.format(timeStamp)
    }

    fun calculatePolylineDistance(polyline: Polyline): Float {
        var distance = 0f
        for (i in 0..polyline.size - 2) {
            val point1 = polyline[i]
            val point2 = polyline[i + 1]
            val result = FloatArray(1)
            Location.distanceBetween(
                point1.latitude,
                point1.longitude,
                point2.latitude,
                point2.longitude,
                result
            )
            distance += result[0]

        }
        return distance
    }




}