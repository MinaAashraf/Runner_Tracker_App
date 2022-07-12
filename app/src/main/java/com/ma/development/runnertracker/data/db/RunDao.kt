package com.ma.development.runnertracker.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ma.development.runnertracker.data.pojo.Run

@Dao
interface RunDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("Select * from running_table order by avrSpeedKMH desc")
    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>>

    @Query("Select * from running_table order by distanceInMeters desc")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    @Query("Select * from running_table order by timeInMillis desc")
    fun getAllRunsSortedByTime(): LiveData<List<Run>>

    @Query("Select * from running_table order by timeStamp desc")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query("Select * from running_table order by burnedCalories desc")
    fun getAllRunsSortedByBurnedCalories(): LiveData<List<Run>>

    @Query("Select SUM(timeInMillis) from running_table")
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query("Select SUM(distanceInMeters) from running_table")
    fun getTotalDistance(): LiveData<Int>

    @Query("Select SUM(avrSpeedKMH) from running_table")
    fun getTotalAvgSpeed(): LiveData<Float>

    @Query("Select AVG(burnedCalories) from running_table")
    fun getTotalBurnedCalories(): LiveData<Int>

}