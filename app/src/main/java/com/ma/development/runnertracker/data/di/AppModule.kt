package com.ma.development.runnertracker.data.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.ma.development.runnertracker.common.Constants.RUNNING_DB_NAME
import com.ma.development.runnertracker.common.Constants.SHARED_PREF_NAME
import com.ma.development.runnertracker.common.Constants.WEIGHT_KEY
import com.ma.development.runnertracker.data.db.RunningDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDb(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        RunningDatabase::class.java,
        RUNNING_DB_NAME
    ).build()

    @Singleton
    @Provides
    fun provideDao(db: RunningDatabase) = db.getDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context) =
        context.getSharedPreferences(SHARED_PREF_NAME, 0)


    @Singleton
    @Provides
    fun provideWeight(sharedPreferences: SharedPreferences) = sharedPreferences.getFloat(WEIGHT_KEY, 60f)

}