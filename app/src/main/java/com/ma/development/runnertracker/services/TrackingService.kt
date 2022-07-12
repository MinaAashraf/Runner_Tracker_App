package com.ma.development.runnertracker.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.ma.development.runnertracker.common.Constants.CHANNEL_NOTIFICATION_ID
import com.ma.development.runnertracker.common.Constants.CHANNEL_NOTIFICATION_NAME
import com.ma.development.runnertracker.common.Constants.NOTIFICATION_ID
import com.ma.development.runnertracker.common.Constants.PAUSE_SERVICE_ACTION
import com.ma.development.runnertracker.common.Constants.START_RESUME_SERVICE_ACTION
import com.ma.development.runnertracker.common.Constants.STOP_SERVICE_ACTION
import com.ma.development.runnertracker.common.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import dagger.multibindings.IntKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    private var isFirstTime = true
    private lateinit var locationCallback: LocationCallback
    private val timeInSeconds = MutableLiveData<Long>()

    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient

    @Inject
    lateinit var currNotificationBuilder: NotificationCompat.Builder

    companion object {
        val pathPoints = MutableLiveData<Polylines>()
        val isTracking = MutableLiveData<Boolean>()
        val timeInMillis = MutableLiveData<Long>()
    }

    private fun initializeLiveData() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeInMillis.postValue(0L)
        timeInSeconds.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        initializeLiveData()
        // callback object
        locationCallback = getLocationCallBack()
        // fusedLocationClientProvider object
        isTracking.observe(this, Observer {
            if (!isServiceKilled) {
                startLocationUpdates()
                updateNotificationTrackingStateAction()
            }
        })


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (intent.action) {

                START_RESUME_SERVICE_ACTION ->
                    if (isFirstTime) {
                        isTracking.postValue(true)
                        startTimer()
                        startForegroundService()
                        isFirstTime = false
                    } else {
                        isTracking.postValue(true)
                        startTimer()
                    }

                PAUSE_SERVICE_ACTION -> pauseService()

                STOP_SERVICE_ACTION -> stopService()

                else -> {}
            }
        }


        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var startingTime = 0L
    private var lapTime = 0L
    private var runTime = 0L
    private var secondsCounter = 1

    private fun startTimer() {
        isTimerEnabled = true
        startingTime = System.currentTimeMillis()
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime = System.currentTimeMillis() - startingTime
                timeInMillis.postValue(runTime + lapTime)
                if (timeInMillis.value!! >= secondsCounter * 1000) {
                    timeInSeconds.postValue(timeInSeconds.value!! + 1)
                    secondsCounter++
                }
                // simple delay
                delay(50)
            }
            runTime += lapTime
        }
    }

    private fun startLocationUpdates() {
        if (isTracking.value!!) {
            addEmptyPolyline()
            if (TrackingUtility.checkLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = 5000L
                    fastestInterval = 2000L
                    priority = Priority.PRIORITY_HIGH_ACCURACY
                }
                fusedLocationClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else
            fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun getLocationCallBack() = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            for (location in result.locations) {
                addPoint(location)
                Log.i(
                    "location",
                    "latitude: ${location.latitude}, longitude: ${location.longitude} "
                )
            }
        }
    }


    private fun addPoint(location: Location?) {
        location?.let {
            pathPoints.value?.apply {
                val latLong = LatLng(location.latitude, location.longitude)
                if (isNotEmpty())
                    last().add(latLong)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() {
        // if pathPoints livedata object:
        // has non nullable polylines -> add empty polyline to the polylines
        // has nullable polylines -> add empty polylines of only one empty polyline

        pathPoints.value?.apply {
            add(mutableListOf())
            pathPoints.postValue(this)
        } ?: pathPoints.postValue(mutableListOf(mutableListOf()))
    }

    private fun startForegroundService() {
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        val notification = currNotificationBuilder.build()

        startForeground(NOTIFICATION_ID, notification)

        timeInSeconds.observe(this) {
            if (!isServiceKilled) {
                val updatedTimeNotificationBuilder =
                    currNotificationBuilder.setContentText(
                        TrackingUtility.getFormattedStopWatchTime(
                            it * 1000L
                        )
                    )

                notificationManager.notify(NOTIFICATION_ID, updatedTimeNotificationBuilder.build())
            }
        }

    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val notificationChannel = NotificationChannel(
            CHANNEL_NOTIFICATION_ID,
            CHANNEL_NOTIFICATION_NAME,
            NotificationManager.IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun updateNotificationTrackingStateAction() {
        val actionTitle = if (isTracking.value!!) "PAUSE" else "RESUME"
        val actionIcon =
            if (isTracking.value!!) com.ma.development.runnertracker.R.drawable.pause_icon else com.ma.development.runnertracker.R.drawable.resume_icon

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent: PendingIntent = if (isTracking.value!!) {
            val pauseIntent = Intent(this, TrackingService::class.java).also {
                it.action = PAUSE_SERVICE_ACTION
            }
            PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).also {
                it.action = START_RESUME_SERVICE_ACTION
            }
            PendingIntent.getService(this, 1, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        currNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        currNotificationBuilder =
            currNotificationBuilder.addAction(actionIcon, actionTitle, pendingIntent)

        notificationManager.notify(NOTIFICATION_ID, currNotificationBuilder.build())

    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private var isServiceKilled = false
    private fun stopService() {
        isServiceKilled = true
        initializeLiveData()
        stopForeground(true)
        stopSelf()
    }

}