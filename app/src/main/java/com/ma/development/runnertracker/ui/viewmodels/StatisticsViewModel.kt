package com.ma.development.runnertracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.ma.development.runnertracker.data.db.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(mainRepository: MainRepository) : ViewModel() {

    val totalRunTime = mainRepository.getTotalTimeInMillis()

    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()

    val totalDistance = mainRepository.getTotalDistance()

    val totalBurnedCalories = mainRepository.getTotalBurnedCalories()

    val sortedRunsByDate = mainRepository.getAllRunsSortedByDate()


}