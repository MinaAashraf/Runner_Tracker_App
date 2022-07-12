package com.ma.development.runnertracker.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.ma.development.runnertracker.R
import com.ma.development.runnertracker.common.Constants
import com.ma.development.runnertracker.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var hostFragment: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.toolbar.title = ""
        setSupportActionBar(binding.toolbar)


        startTrackingFragmentIfNeeded(intent)

        hostFragment = findViewById(R.id.host_fragment)
        binding.navBar.setupWithNavController(hostFragment!!.findNavController())

        hostFragment!!.findNavController().addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.runFragment, R.id.statisticsFragment, R.id.settingsFragment -> binding.navBar.visibility =
                    View.VISIBLE
                else -> binding.navBar.visibility = View.GONE
            }
        }
    }

    suspend fun netCall1(): String {
        delay(1000)
        return "Mina"
    }

    suspend fun netCall2(): String {
        delay(1000)
        return "Ashraf"

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        startTrackingFragmentIfNeeded(intent)
    }

    private fun startTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == Constants.FOREGROUND_SERVICE_PENDING_ACTION) {
            hostFragment?.let {
                it.findNavController().navigate(R.id.action_anyFragment_to_trackerFragment)
            } ?: kotlin.run {
                hostFragment = findViewById(R.id.host_fragment)
                hostFragment?.findNavController()
                    ?.navigate(R.id.action_anyFragment_to_trackerFragment)
            }
        }
    }

}