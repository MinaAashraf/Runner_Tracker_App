package com.ma.development.runnertracker.ui.fragments

import android.media.Image
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ma.development.runnertracker.R
import com.ma.development.runnertracker.adapters.RunAdapter
import com.ma.development.runnertracker.common.Constants.LOCATION_PERMISSION_REQUEST_CODE
import com.ma.development.runnertracker.common.SortType
import com.ma.development.runnertracker.common.TrackingUtility
import com.ma.development.runnertracker.data.pojo.Run
import com.ma.development.runnertracker.databinding.FragmentRunBinding
import com.ma.development.runnertracker.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private val viewModel: MainViewModel by viewModels()
    private var sortType = SortType.DATE
    private var runList: List<Run> = listOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentRunBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_run, container, false)
        binding.lifecycleOwner = this
        // ask user to open location permission
        requestLocationPermissions()

        binding.sortImageBtn.setOnClickListener { showMenu(it) }
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackerFragment)
        }
        val runAdapter = RunAdapter(requireContext()).apply {
            viewModel.sortedRuns.observe(viewLifecycleOwner) {
                runList = it
                sortRuns(this)
            }
            viewModel.sortType.observe(viewLifecycleOwner) {
                sortType = it
                if (runList.isNotEmpty())
                    sortRuns(this)
            }
        }

        binding.runRecyclerView.adapter = runAdapter

        //   setHasOptionsMenu(true)
        return binding.root
    }

    private fun sortRuns(adapter: RunAdapter) {
        when (sortType) {
            SortType.DATE -> adapter.submitDataList(runList.sortedByDescending { it.timeStamp })
            SortType.RUNTIME -> adapter.submitDataList(runList.sortedByDescending { it.timeInMillis })
            SortType.DISTANCE -> adapter.submitDataList(runList.sortedByDescending { it.distanceInMeters })
            SortType.AVGSPEED -> adapter.submitDataList(runList.sortedByDescending { it.avrSpeedKMH })
            SortType.CALORIES -> adapter.submitDataList(runList.sortedByDescending { it.burnedCalories })
        }
    }

    private fun requestLocationPermissions() {
        if (TrackingUtility.checkLocationPermissions(requireActivity()))
            return
        TrackingUtility.requestLocationPermissions(this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            TrackingUtility.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
            LOCATION_PERMISSION_REQUEST_CODE,
            permissions,
            grantResults,
            this
        )
    }

    /* override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
         super.onCreateOptionsMenu(menu, inflater)
         inflater.inflate(R.menu.menu_run, menu)


     }

     override fun onPrepareOptionsMenu(menu: Menu) {
         val imageBtn  = menu.findItem(R.id.sort_icon).actionView as ImageButton
         imageBtn.setOnClickListener{
             Toast.makeText(requireContext(),"work",Toast.LENGTH_LONG).show()
         }
         super.onPrepareOptionsMenu(menu)

     }*/

    /*  override fun onOptionsItemSelected(item: MenuItem): Boolean {
          return when (item.itemId) {
              R.id.sort_icon -> {
                  showMenu(item.actionView)
                  true
              }
              else -> super.onOptionsItemSelected(item)
          }
      }
  */
    private fun showMenu(v: View) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(R.menu.popup_sorting_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            changeSortType(menuItem.itemId)
            true
        }
        popup.show()
    }

    private fun changeSortType(itemId: Int) {
        when (itemId) {
            R.id.srtByDateItem -> viewModel.sortRuns(SortType.DATE)
            R.id.srtByDurationItem -> viewModel.sortRuns(SortType.RUNTIME)
            R.id.srtBySpeedItem -> viewModel.sortRuns(SortType.AVGSPEED)
            R.id.srtByDistanceItem -> viewModel.sortRuns(SortType.DISTANCE)
            R.id.srtByCaloriesItem -> viewModel.sortRuns(SortType.CALORIES)
        }
    }

}