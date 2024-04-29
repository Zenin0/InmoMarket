package com.isanz.inmomarket.ui.search


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.isanz.inmomarket.R
import com.isanz.inmomarket.utils.Constants

@Suppress("DEPRECATION")
class SearchFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var searchViewModel: SearchViewModel
    private var allowUbication: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        searchViewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        setUpMap()
        return view
    }

    private fun setUpMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val sharedPref =
            requireActivity().getSharedPreferences("settings_preferences", Context.MODE_PRIVATE)
        allowUbication = sharedPref.getBoolean("allowUbication", false)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), Constants.LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        enableUserLocation()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == Constants.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableUserLocation() {
        if (allowUbication) {
            mMap.isMyLocationEnabled = true
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                    searchViewModel.getLatAndLong().observe(viewLifecycleOwner) { locations ->
                        locations?.forEach { latLongIdPairs ->
                            val markerLatLng = LatLng(latLongIdPairs.second, latLongIdPairs.third)
                            val customMarker = BitmapDescriptorFactory.fromBitmap(resizeBitmap())
                            val marker = mMap.addMarker(
                                MarkerOptions().position(markerLatLng).icon(customMarker)
                            )
                            marker?.tag = latLongIdPairs.first
                        }
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "Ubication not allowed", Toast.LENGTH_SHORT).show()
        }

        mMap.setOnMarkerClickListener { marker ->
            val args = Bundle()
            args.putString("propertyId", marker.tag as String)
            findNavController().navigate(R.id.navigation_mini_property, args)
            false
        }

    }

    private fun resizeBitmap(): Bitmap {
        val imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.house)
        return Bitmap.createScaledBitmap(imageBitmap, 100, 100, false)
    }
}