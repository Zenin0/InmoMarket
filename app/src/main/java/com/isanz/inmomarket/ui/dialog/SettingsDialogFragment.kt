package com.isanz.inmomarket.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.isanz.inmomarket.R

class SettingsDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = MaterialAlertDialogBuilder(it)
            builder.setMessage("Biometric login is available?")
                .setPositiveButton("Want to enable it?"
                ) { _, _ ->
                    findNavController().navigate(R.id.action_navigation_home_to_navigation_settings)
                }
                .setNegativeButton("Cancel"
                ) { dialog, _ ->
                    dialog.dismiss()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}