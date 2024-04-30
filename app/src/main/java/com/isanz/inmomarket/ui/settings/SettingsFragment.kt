package com.isanz.inmomarket.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
        val sharedPref = getSharedPref()
        val biometricLogin = sharedPref.getBoolean("biometricLogin", false)
        val allowUbication = sharedPref.getBoolean("allowUbication", false)
        mBinding.sBiometricalLogin.isChecked = biometricLogin
        mBinding.sAllowUbication.isChecked = allowUbication
    }

    private fun getSharedPref() =
        requireActivity().getSharedPreferences("settings_preferences", Context.MODE_PRIVATE)

    private fun setUpButtons() {
        setUpBiometricLoginButton()
        setUpBackButton()
        setUpCloseAccountButton()
        setUpAllowUbicationButton()
        setUpChangePasswordButton()
    }

    private fun setUpBiometricLoginButton() {
        mBinding.sBiometricalLogin.setOnCheckedChangeListener { _, isChecked ->
            val sharedPref = getSharedPref()
            with(sharedPref.edit()) {
                putBoolean("biometricLogin", isChecked)
                apply()
            }
        }
    }

    private fun setUpBackButton() {
        mBinding.ibBack.setOnClickListener {
            this.findNavController().popBackStack()
        }
    }

    private fun setUpCloseAccountButton() {
        mBinding.btnCloseAccount.setOnClickListener {
            mViewModel.closeAccount()
        }
    }

    private fun setUpAllowUbicationButton() {
        mBinding.sAllowUbication.setOnClickListener {
            val sharedPref = getSharedPref()
            with(sharedPref.edit()) {
                putBoolean("allowUbication", mBinding.sAllowUbication.isChecked)
                apply()
            }
        }
    }

    private fun setUpChangePasswordButton() {
        mBinding.btnChangePassword.setOnClickListener {
            InmoMarket.getAuth().currentUser?.let {
                InmoMarket.getAuth().sendPasswordResetEmail(it.email!!)
            }
            Toast.makeText(requireContext(), "Email sent", Toast.LENGTH_SHORT).show()
        }
    }
}