package com.ma.development.runnertracker.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.gms.maps.model.Marker
import com.ma.development.runnertracker.R
import com.ma.development.runnertracker.data.pojo.Run
import com.ma.development.runnertracker.databinding.FragmentStatisticsBinding
import com.ma.development.runnertracker.ui.viewmodels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round


@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private val viewModel: StatisticsViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentStatisticsBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_statistics, container, false)
        binding.lifecycleOwner = this
        binding.vm = viewModel
        subscribeToObserver(binding)
        setupBarChart(binding.barChart)
        return binding.root
    }

    private fun setupBarChart(barChart: BarChart) {
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = resources.getColor(R.color.grey)
            textColor = resources.getColor(R.color.grey)
            setDrawGridLines(false)
        }
        barChart.axisLeft.apply {
            axisLineColor = resources.getColor(R.color.grey)
            textColor = resources.getColor(R.color.grey)
            setDrawGridLines(false)
        }
        barChart.axisRight.apply {
            axisLineColor = resources.getColor(R.color.grey)
            textColor = resources.getColor(R.color.grey)
            setDrawGridLines(false)
        }
        barChart.apply {
            description.text = "Avg Speed Over Time"
            legend.isEnabled = false
        }
    }

    private fun subscribeToObserver(binding: FragmentStatisticsBinding) {
        viewModel.sortedRunsByDate.observe(viewLifecycleOwner) {
            it?.let {
                val allAvGSpeeds: List<BarEntry> =
                    it.indices.map { i -> BarEntry(i.toFloat(), it[i].avrSpeedKMH) }
                val barDataset = BarDataSet(allAvGSpeeds, "All Speed Over Time").apply {
                    valueTextColor = resources.getColor(R.color.grey)
                    color = resources.getColor(R.color.purple_500)
                }
                binding.barChart.data = BarData(barDataset)
                //                binding.barChart.marker = MarkerView(it.reversed(), requireContext())
                binding.barChart.invalidate()
            }
        }
    }

}