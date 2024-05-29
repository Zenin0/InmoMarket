package com.isanz.inmomarket.ui.add

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.isanz.inmomarket.R
import com.isanz.inmomarket.databinding.FragmentAddAdressBinding
import com.isanz.inmomarket.utils.Constants


class AddAdressFragment : Fragment() {

    private lateinit var mBinding: FragmentAddAdressBinding
    private val addViewModel: AddViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentAddAdressBinding.inflate(inflater, container, false)
        setUp()
        return mBinding.root
    }

    private fun setUp() {
        initializePlaces()
        setupAddressClickListener()
        setUpButtons()
        setUpDrawables()
    }

    private fun setUpButtons() {
        mBinding.btnNext.setOnClickListener {
            validateFields(mBinding.tieAddress.text.toString(), mBinding.tieBats.text.toString(), mBinding.tieRooms.text.toString(), mBinding.tieSquareMeters.text.toString(), mBinding.tieFloors.text.toString())
        }
    }

    private fun validateFields(location : String, baths : String, rooms : String, meters : String, floors : String) {
        var isValid = true

        if (location.isEmpty()) {
            mBinding.tieAddress.error = getString(R.string.location_is_required)
            isValid = false
        }
        if (rooms.isEmpty()) {
            mBinding.tieRooms.error = getString(R.string.rooms_is_required)
            return
        }
        if (rooms.toInt() == 0) {
            mBinding.tieRooms.error = getString(R.string.rooms_is_required)
            isValid = false
        }
        if (baths.isEmpty()) {
            mBinding.tieBats.error = getString(R.string.baths_is_required)
            return
        }
        if (baths.toInt() == 0) {
            mBinding.tieBats.error = getString(R.string.baths_is_required)
            isValid = false
        }
        if (meters.isEmpty()) {
            mBinding.tieSquareMeters.error = getString(R.string.square_meters_is_required)
            return
        }
        if (meters.toInt() == 0) {
            mBinding.tieSquareMeters.error = getString(R.string.square_meters_is_required)
            isValid = false
        }
        if (floors.isEmpty()) {
            mBinding.tieFloors.error = getString(R.string.floors_is_required)
            return
        }
        if (floors.toInt() == 0) {
            mBinding.tieFloors.error = getString(R.string.floors_is_required)
            isValid = false
        }
        if (isValid) {
            prepareForNextFragment()
        }
    }

    private fun setUpDrawables() {
        val drawableBath = ContextCompat.getDrawable(requireContext(), R.drawable.ic_bathroom)
        val drawableRooms = ContextCompat.getDrawable(requireContext(), R.drawable.ic_bedroom)
        val drawableMeters = ContextCompat.getDrawable(requireContext(), R.drawable.ic_square_foot)
        val drawableFloor = ContextCompat.getDrawable(requireContext(), R.drawable.ic_house_siding)
        mBinding.tieBats.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableBath, null)
        mBinding.tieRooms.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableRooms, null)
        mBinding.tieSquareMeters.setCompoundDrawablesWithIntrinsicBounds(
            null, null, drawableMeters, null
        )
        mBinding.tieFloors.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableFloor, null)
    }

    private fun prepareForNextFragment() {
        val property = addViewModel.property.value
        if (property != null) {
            property.location = mBinding.tieAddress.text.toString()
        }
        val extras = mapOf(
            "rooms" to mBinding.tieRooms.text.toString().toInt(),
            "baths" to mBinding.tieBats.text.toString().toInt(),
            "squareMeters" to mBinding.tieSquareMeters.text.toString().toInt(),
            "floors" to mBinding.tieFloors.text.toString().toInt(),
        )
        if (property != null) {
            property.extras = extras
        }
        property?.let { addViewModel.setProperty(it) }
        navigateToNextFragment()

    }

    private fun navigateToNextFragment() {
        findNavController().navigate(R.id.action_navigation_add_adress_to_navigation_add_images)
    }

    private fun initializePlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), Constants.API_GOOGLE)
        }
    }

    private fun setupAddressClickListener() {
        mBinding.tieAddress.setOnClickListener {
            launchPlaceAutocomplete()
        }
    }

    private fun launchPlaceAutocomplete() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .build(requireContext())
        placeAutocompleteLauncher.launch(intent)
    }

    private val placeAutocompleteLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            handlePlaceAutocompleteResult(result.data!!)
        }
    }

    private fun handlePlaceAutocompleteResult(data: Intent) {
        val place = Autocomplete.getPlaceFromIntent(data)
        mBinding.tieAddress.setText(place.address)
    }


}