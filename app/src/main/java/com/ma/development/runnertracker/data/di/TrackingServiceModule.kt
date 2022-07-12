package com.ma.development.runnertracker.data.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.ma.development.runnertracker.R
import com.ma.development.runnertracker.common.Constants
import com.ma.development.runnertracker.ui.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped


@Module
@InstallIn(ServiceComponent::class)
class TrackingServiceModule {

    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context) =
        LocationServices.getFusedLocationProviderClient(context)

    @ServiceScoped
    @Provides
    fun providePendingIntent(@ApplicationContext context: Context) = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).also {
            it.action = Constants.FOREGROUND_SERVICE_PENDING_ACTION
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(
        context,
        Constants.CHANNEL_NOTIFICATION_ID,
    ).setContentTitle(context.getString(R.string.app_name))
        .setContentText("00:00:00")
        .setSmallIcon(R.drawable.runner_icon)
        .setContentIntent(pendingIntent)
        .setAutoCancel(false)
        .setOngoing(true)


}