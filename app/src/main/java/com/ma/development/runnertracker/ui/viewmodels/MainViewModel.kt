package com.ma.development.runnertracker.ui.viewmodels

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.ma.development.runnertracker.common.SortType
import com.ma.development.runnertracker.common.TrackingUtility
import com.ma.development.runnertracker.data.db.MainRepository
import com.ma.development.runnertracker.data.pojo.Run
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    val sortedRuns: LiveData<List<Run>> = mainRepository.getAllRunsSortedByDate()

    private val _sortType = MutableLiveData(SortType.DATE)
    val sortType: LiveData<SortType> = _sortType

    fun insertRun(run: Run) {
        viewModelScope.launch {
            mainRepository.insertRun(run)
        }
    }

    fun sortRuns(sortType: SortType) {
        this._sortType.value = sortType
    }


    fun getLastLocation(fusedClient: FusedLocationProviderClient): Task<Location> {
        return fusedClient.lastLocation
    }


}