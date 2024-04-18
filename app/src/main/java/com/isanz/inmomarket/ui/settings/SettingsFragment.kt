package com.isanz.inmomarket.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.isanz.inmomarket.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var mBinding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        mBinding = FragmentSettingsBinding.inflate(layoutInflater)
        setUpView()
        return mBinding.root
    }

    private fun setUpView() {
        retrieveSettings()
        setUpButtons()
    }

    private fun retrieveSettings() {
        // Retrieve the value from SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("settings_preferences", Context.MODE_PRIVATE)
        val biometricLogin = sharedPref.getBoolean("biometricLogin", false)
        mBinding.switchBiometricLogin.isChecked = biometricLogin
    }

    private fun setUpButtons() {
        mBinding.switchBiometricLogin.setOnCheckedChangeListener { _, isChecked ->
            // Save the value in SharedPreferences
            val sharedPref =
                requireActivity().getSharedPreferences("settings_preferences", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putBoolean("biometricLogin", isChecked)
                apply()
            }
        }
    }
}