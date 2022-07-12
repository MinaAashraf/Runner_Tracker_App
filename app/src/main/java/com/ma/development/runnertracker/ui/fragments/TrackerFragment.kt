package com.ma.development.runnertracker.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.ma.development.runnertracker.R
import com.ma.development.runnertracker.common.Constants.PAUSE_SERVICE_ACTION
import com.ma.development.runnertracker.common.Constants.START_RESUME_SERVICE_ACTION
import com.ma.development.runnertracker.common.Constants.STOP_SERVICE_ACTION
import com.ma.development.runnertracker.common.TrackingUtility
import com.ma.development.runnertracker.data.pojo.Run
import com.ma.development.runnertracker.databinding.FragmentTrackerBinding
import com.ma.development.runnertracker.services.Polyline
import com.ma.development.runnertracker.services.TrackingService
import com.ma.development.runnertracker.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.round

@AndroidEntryPoint
class TrackerFragment : Fragment() {

    @Inject
    @Named("lastLocation")
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentTrackerBinding
    private var gMap: GoogleMap? = null
    private lateinit var mapView: MapView
    private var pathPoints = mutableListOf<Polyline>()
    private var isTracking: Boolean = false
    private var timeInMillis: Long = 0L
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_tracker, container, false)

        binding.lifecycleOwner = viewLifecycleOwner

        mapView = binding.mapView

        mapView.onCreate(savedInstanceState)


        mapView.getMapAsync {
            gMap = it
            gMap?.setMapStyle(MapStyleOptions(resources.getString(R.string.style_json)))
            putLastLocation(gMap!!)
            drawAllPolylines()
            animateCamera()
        }

        binding.toggleBtn.setOnClickListener {
            toggle()
        }
        subscribeToObservers()
        binding.finishBtn.setOnClickListener { finishRun() }
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun putLastLocation(gMap: GoogleMap) {
        if (TrackingUtility.checkLocationPermissions(requireContext())) {
            val task = viewModel.getLastLocation(fusedLocationProviderClient)
            task.addOnSuccessListener {
                gMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.latitude, it.longitude), 18f
                    )
                )
            }
        }
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            isTracking = it
            Toast.makeText(requireContext(), isTracking.toString(), Toast.LENGTH_LONG).show()
            updateButtons()
        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            drawLatestPolyline()
            animateCamera()
        })

        TrackingService.timeInMillis.observe(viewLifecycleOwner, Observer {
            timeInMillis = it
            binding.stopWatchTxt.text = TrackingUtility.getFormattedStopWatchTime(it, true)
        })
    }

    private fun drawAllPolylines() {
        if (pathPoints.isNotEmpty()) {
            var polyline: PolylineOptions? = null
            pathPoints.forEach {
                polyline = PolylineOptions().apply {
                    color(R.color.purple_700)
                    width(7f)
                    if (it.isNotEmpty())
                        addAll(it)
                }
                gMap?.addPolyline(polyline!!)
            }
        }
    }

    private fun drawLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val polyline = PolylineOptions().apply {
                color(R.color.purple_700)
                width(7f)
                add(pathPoints.last()[pathPoints.last().size - 2])
                add(pathPoints.last().last())
            }
            gMap?.addPolyline(polyline)
        }
    }

    private fun animateCamera() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty())
            gMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(pathPoints.last().last(), 18f)
            )
    }

    private fun toggle() {
        if (!isTracking)
            sendCommandToTrackingService(START_RESUME_SERVICE_ACTION)
        else
            sendCommandToTrackingService(PAUSE_SERVICE_ACTION)
    }

    private fun updateButtons() {
        if (isTracking) {
            binding.toggleBtn.text = "Stop"
            binding.finishBtn.visibility = View.GONE
        } else {
            binding.toggleBtn.text = "Start"
            binding.finishBtn.visibility = View.VISIBLE
        }
    }


    fun sendCommandToTrackingService(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireActivity().startService(it)
        }
    }

    private fun finishRun(save: Boolean = true) {
        if (save) {
            zoomToTotalPath()
            saveRunToDb()
        }
        sendCommandToTrackingService(STOP_SERVICE_ACTION)
        findNavController().navigate(R.id.action_trackerFragment_to_runFragment)
    }

    private fun zoomToTotalPath() {
        val bounds = LatLngBounds.builder()
        for (polyline in pathPoints)
            for (point in polyline) {
                bounds.include(point)
            }
        gMap?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                mapView.height * 0.05f.toInt()
            )
        )
    }

    @set:Inject
    var weight = 60f
    private fun saveRunToDb() {
        var totalDistanceInMeters = 0f
        gMap?.snapshot {
            for (polyline in pathPoints) {
                totalDistanceInMeters += TrackingUtility.calculatePolylineDistance(polyline)
            }
            val avgSpeedInKMH =
                round((totalDistanceInMeters / 1000) / (timeInMillis / 1000f / 60 / 60) / 10) * 10f
            val runtimeStamp = Calendar.getInstance().timeInMillis
            val burnedCalories = round((totalDistanceInMeters / 1000) * weight).toInt()
            val run = Run(
                avgSpeedInKMH,
                round(totalDistanceInMeters).toInt(),
                timeInMillis,
                runtimeStamp,
                burnedCalories,
                it
            )
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run data is saved",
                Snackbar.LENGTH_LONG
            )
        }

    }

    // the following callbacks implementations to handle the map view life cycle manually:
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


    private fun showCancelRunDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel the run?")
            .setMessage("Are you sure to cancel the current run?")
            .setIcon(R.drawable.delete_icon)
            .setPositiveButton("Yes") { _, _ ->
                finishRun(false)
            }.setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.create().show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.remove_run_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (timeInMillis > 0)
            menu.getItem(0)?.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.remove_icon -> {
                removeRun()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun removeRun() {
        showCancelRunDialog()
    }

}