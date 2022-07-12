package com.ma.development.runnertracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ma.development.runnertracker.R
import com.ma.development.runnertracker.common.Constants
import com.ma.development.runnertracker.databinding.FragmentRunBinding
import com.ma.development.runnertracker.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentSettingsBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_settings, container, false)
        binding.lifecycleOwner = this
        binding.textInput.editText?.setText(getWeight().toString())
        binding.enterBtn.setOnClickListener {
            changeWeight(binding.textInput.editText)
        }
        return binding.root
    }

    private fun getWeight() = sharedPref.getFloat(Constants.WEIGHT_KEY, 60f)

    private fun changeWeight(editText: EditText?) {
        editText?.let {
            if (it.text!!.isEmpty())
                editText.error = "Weight is required"
            else {
                saveWeightToPrefs(editText.text.toString().toFloat())
            }
        }
    }

    @Inject
    lateinit var sharedPref: SharedPreferences
    private fun saveWeightToPrefs(weight: Float) {
        sharedPref.edit().putFloat(Constants.WEIGHT_KEY, weight).apply()
        Toast.makeText(requireContext(), "Your new weight is $weight Kg", Toast.LENGTH_SHORT).show()
    }

}