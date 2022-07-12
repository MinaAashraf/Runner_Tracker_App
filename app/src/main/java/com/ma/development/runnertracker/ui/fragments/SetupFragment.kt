package com.ma.development.runnertracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.ma.development.runnertracker.R
import com.ma.development.runnertracker.common.Constants
import com.ma.development.runnertracker.databinding.FragmentSetupBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentSetupBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_setup, container, false)
        binding.lifecycleOwner = this
        checkIfNotFirstTime(savedInstanceState)

        binding.enterBtn.setOnClickListener {
            binding.textInput.editText?.let {
                if (it.text.isEmpty())
                    binding.textInput.editText?.error = "Weight is required"
                else {
                    saveWeightToPrefs(it.text.toString().toFloat())
                    findNavController().navigate(R.id.action_setupFragment_to_runFragment)
                }
            }
        }
        return binding.root
    }

   private fun checkIfNotFirstTime(savedInstanceState: Bundle?) {
        if (sharedPreferences.contains(Constants.WEIGHT_KEY)) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }

    }

   private fun saveWeightToPrefs(weight: Float) {
        sharedPreferences.edit().putFloat(Constants.WEIGHT_KEY, weight).apply()
    }
}