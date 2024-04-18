package com.isanz.inmomarket.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var mBinding: FragmentSettingsBinding
    private lateinit var mViewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        mBinding = FragmentSettingsBinding.inflate(layoutInflater)
        mViewModel = SettingsViewModel()
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
        val allowUbication = sharedPref.getBoolean("allowUbication", false)
        mBinding.sBiometricalLogin.isChecked = biometricLogin
        mBinding.sAllowUbication.isChecked = allowUbication
    }

    private fun setUpButtons() {
        mBinding.sBiometricalLogin .setOnCheckedChangeListener { _, isChecked ->
            // Save the value in SharedPreferences
            val sharedPref =
                requireActivity().getSharedPreferences("settings_preferences", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putBoolean("biometricLogin", isChecked)
                apply()
            }
        }
        mBinding.ibBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        mBinding.btnCloseAccount.setOnClickListener{
            mViewModel.closeAccount()
        }

        mBinding.sAllowUbication.setOnClickListener{
            // Save the value in SharedPreferences
            val sharedPref =
                requireActivity().getSharedPreferences("settings_preferences", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putBoolean("allowUbication", mBinding.sAllowUbication.isChecked)
                apply()
            }
        }
        mBinding.btnChangePassword.setOnClickListener {
            InmoMarket.getAuth().currentUser?.let {
                InmoMarket.getAuth().sendPasswordResetEmail(it.email!!)
            }
            Toast.makeText(requireContext(), "Email sent", Toast.LENGTH_SHORT).show()
        }
    }
}