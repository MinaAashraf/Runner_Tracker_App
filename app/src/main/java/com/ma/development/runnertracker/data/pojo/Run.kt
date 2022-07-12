package com.ma.development.runnertracker.data.pojo

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table")
data class Run(
    var avrSpeedKMH : Float = 0f,
    var distanceInMeters : Int = 0,
    var timeInMillis : Long = 0L,
    var timeStamp : Long = 0L,
    var burnedCalories : Int = 0,
    var img : Bitmap? = null
){
    @PrimaryKey(autoGenerate = true)
    var key : Int? = null
}
