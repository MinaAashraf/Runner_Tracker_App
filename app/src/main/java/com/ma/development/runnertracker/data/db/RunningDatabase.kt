package com.ma.development.runnertracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ma.development.runnertracker.data.pojo.Run

@Database(
    entities = [Run::class],
    version = 1
)
@TypeConverters (Converters::class)
abstract class RunningDatabase : RoomDatabase() {

    abstract fun getDao () : RunDao
}