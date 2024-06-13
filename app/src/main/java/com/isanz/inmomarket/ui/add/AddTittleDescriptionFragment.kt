package com.isanz.inmomarket.ui.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.isanz.inmomarket.R
import com.isanz.inmomarket.databinding.FragmentAddTittleDescriptionBinding
import com.isanz.inmomarket.utils.entities.Property


class AddTittleDescriptionFragment : Fragment() {


    private lateinit var mBinding: FragmentAddTittleDescriptionBinding
    private val addViewModel: AddViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentAddTittleDescriptionBinding.inflate(inflater, container, false)
        setUp()
        return mBinding.root
    }

    private fun setUp() {
        setUpButtons()
    }

    private fun setUpButtons() {
        mBinding.btnNext.setOnClickListener {
            validateFields(
                mBinding.tilTittle.editText?.text.toString(),
                mBinding.tilDescription.editText?.text.toString()
            )

        }
    }

    private fun validateFields(tittle: String, description: String) {
        var isValid = true
        if (tittle.isEmpty()) {
            mBinding.tieTittle.error = getString(R.string.tittle_is_required)
            isValid = false
        }
        if (tittle.length < 5) {
            mBinding.tieTittle.error = getString(R.string.tittle_must_be_at_least_5_characters)
            isValid = false
        }
        if (tittle.length > 10) {
            mBinding.tieTittle.error = getString(R.string.tittle_must_be_at_most_10_characters)
            isValid = false
        }
        if (description.isEmpty()) {
            mBinding.tieDescription.error = getString(R.string.description_is_required)
            isValid = false
        }
        if (description.length < 100) {
            mBinding.tieDescription.error = getString(R.string.description_least_100)
            isValid = false
        }
        if (description.length > 300) {
            mBinding.tieDescription.error = getString(R.string.description_most_300)
            isValid = false
        }
        if (isValid) {
            prepareForNextFragment()
        }
    }

    private fun navigateToNextFragment() {
        findNavController().navigate(R.id.action_navigation_add_tittle_description_to_navigation_add_adress)
    }

    private fun prepareForNextFragment() {
        val property = Property()
        property.tittle = mBinding.tieTittle.text.toString()
        property.description = mBinding.tieDescription.text.toString()
        addViewModel.setProperty(property)
        navigateToNextFragment()
    }


}
