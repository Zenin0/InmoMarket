package com.isanz.inmomarket.ui.search


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

class SearchFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var allowUbication: Boolean = false
    private val searchViewModel: SearchViewModel by lazy {
        ViewModelProvider(this)[SearchViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        setUpMap()
        return view
    }

    private fun setUpMap() {
        initializeMapFragment()
        initializeFusedLocationClient()
        retrieveUbicationSetting()
    }

    private fun initializeMapFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initializeFusedLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun retrieveUbicationSetting() {
        val sharedPref =
            requireActivity().getSharedPreferences("settings_preferences", Context.MODE_PRIVATE)
        allowUbication = sharedPref.getBoolean("allowUbication", false)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkLocationPermission()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationPermissionGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (locationPermissionGranted) {
            enableUserLocation()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }
        enableUserLocation()
    }

    private fun enableUserLocation() {
        if (allowUbication) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
                return
            }
            mMap.isMyLocationEnabled = true
            mMap.isMyLocationEnabled = true
            setMapLocationToUserLocation()
        } else {
            Toast.makeText(requireContext(), "Ubication not allowed", Toast.LENGTH_SHORT).show()
        }

        setMarkerClickListener()
    }

    private fun setMapLocationToUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }
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
    }

    private fun setMarkerClickListener() {
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