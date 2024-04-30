package com.isanz.inmomarket.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.Constants
import com.isanz.inmomarket.utils.retrofit.GeocodingResponse
import com.isanz.inmomarket.utils.retrofit.GeocodingService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Collections

class SearchViewModel : ViewModel() {

    private val db = InmoMarket.getDb()
    private val retrofit = createRetrofit()
    private val geocodingService = createGeocodingService()

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder().baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    private fun createGeocodingService(): GeocodingService {
        return retrofit.create(GeocodingService::class.java)
    }

    private fun getAllAdresses(): LiveData<List<Pair<String, String>>> {
        try {
            val addressesLiveData = MutableLiveData<List<Pair<String, String>>>()
            val currentUserId = InmoMarket.getAuth().currentUser!!.uid

            db.collection("properties").get().addOnSuccessListener { result ->
                val addressesList = result.documents.mapNotNull { document ->
                    val userId = document.getString("userId")
                    val location = document.getString("location")
                    if (userId != currentUserId && location != null) Pair(
                        document.id, location
                    ) else null
                }
                addressesLiveData.value = addressesList
            }.addOnFailureListener { exception ->
                Log.w(Constants.TAG, "Error getting documents.", exception)
            }

            return addressesLiveData
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Error getting addresses.", e)
            return MutableLiveData()
        }
    }

    fun getLatAndLong(): LiveData<List<Triple<String, Double, Double>>> {
        try {
            val locationsLiveData = MutableLiveData<List<Triple<String, Double, Double>>>()
            val locationsList =
                Collections.synchronizedList(ArrayList<Triple<String, Double, Double>>())

            getAllAdresses().observeForever { addresses ->
                addresses?.forEach { (id, address) ->
                    getLatLngForAddress(id, address, locationsList, locationsLiveData)
                }
            }

            return locationsLiveData
        } catch (e: Exception) {
            Log.e(Constants.TAG, "getLatAndLong:failure.", e)
            return MutableLiveData()
        }
    }

    private fun getLatLngForAddress(
        id: String,
        address: String,
        locationsList: MutableList<Triple<String, Double, Double>>,
        locationsLiveData: MutableLiveData<List<Triple<String, Double, Double>>>
    ) {
        try {
            geocodingService.getLatLng(address, Constants.API_GOOGLE)
                .enqueue(object : Callback<GeocodingResponse> {
                    override fun onResponse(
                        call: Call<GeocodingResponse>, response: Response<GeocodingResponse>
                    ) {
                        if (response.isSuccessful) {
                            val geocodingResponse = response.body()
                            val location = geocodingResponse?.results?.get(0)?.geometry?.location
                            if (location != null) {
                                locationsList.add(Triple(id, location.lat, location.lng))
                                locationsLiveData.value = locationsList
                            }
                        }
                    }

                    override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                        Log.w(Constants.TAG, "getLatLngForAdress:failure", t)
                    }
                })
        } catch (e: Exception) {
            Log.e(Constants.TAG, "getLatLngForAdress:failure", e)
        }
    }
}