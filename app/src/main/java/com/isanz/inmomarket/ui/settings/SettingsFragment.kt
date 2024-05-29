package com.isanz.inmomarket.ui.settings

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.R
import com.isanz.inmomarket.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var mBinding: FragmentSettingsBinding
    private val settingsViewModel: SettingsViewModel by lazy {
        ViewModelProvider(this)[SettingsViewModel::class.java]
    }

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
        setUpChangeUsername()
    }

    private fun setUpChangeUsername() {
        mBinding.btnChangeUsername.setOnClickListener {
            val layout = TextInputLayout(requireContext())
            val input = TextInputEditText(requireContext()).apply {
                inputType = InputType.TYPE_CLASS_TEXT
                hint = getString(R.string.prompt_username)
            }

            layout.addView(input)

            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.change_username))
                .setView(layout)
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    val newUsername = input.text.toString()
                    settingsViewModel.changeUsername(newUsername)
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.cancel()
                }
                .show()

            layout.layoutParams = (layout.layoutParams as ViewGroup.MarginLayoutParams).apply {
                setMargins(50, 0, 50, 0) // Set margins as needed
            }

            dialog.show()
        }
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
            settingsViewModel.closeAccount()
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
        try {
            mBinding.btnChangePassword.setOnClickListener {
                InmoMarket.getAuth().currentUser?.let {
                    InmoMarket.getAuth().sendPasswordResetEmail(it.email!!)
                }
                Toast.makeText(requireContext(), "Email sent", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
