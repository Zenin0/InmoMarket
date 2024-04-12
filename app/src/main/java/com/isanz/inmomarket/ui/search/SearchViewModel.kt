package com.isanz.inmomarket.ui.search

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.Constants
import com.isanz.inmomarket.utils.retrofit.GeocodingResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Collections

class SearchViewModel : ViewModel() {

    private val db = InmoMarket.getDb()
    private val retrofit = Retrofit.Builder().baseUrl("https://maps.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create()).build()

    private val geocodingService = retrofit.create(GeocodingService::class.java)

    private fun getAllAdresses(): LiveData<List<String>> {
        val addressesLiveData = MutableLiveData<List<String>>()

        db.collection("properties").get().addOnSuccessListener { result ->
            val addressesList = result.documents.mapNotNull { document ->
                document.getString("location") // replace "address" with the actual field name in your Firestore documents
            }
            addressesLiveData.value = addressesList
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error getting documents.", exception)
        }

        return addressesLiveData
    }

    fun getLatAndLong(): LiveData<List<Pair<Double, Double>>> {
        val locationsLiveData = MutableLiveData<List<Pair<Double, Double>>>()
        val locationsList = Collections.synchronizedList(ArrayList<Pair<Double, Double>>())

        getAllAdresses().observeForever { addresses ->
            addresses?.forEach { address ->
                geocodingService.getLatLng(address, Constants.API_GOOGLE)
                    .enqueue(object : Callback<GeocodingResponse> {
                        override fun onResponse(
                            call: Call<GeocodingResponse>, response: Response<GeocodingResponse>
                        ) {
                            if (response.isSuccessful) {
                                val geocodingResponse = response.body()
                                val location =
                                    geocodingResponse?.results?.get(0)?.geometry?.location
                                if (location != null) {
                                    locationsList.add(Pair(location.lat, location.lng))
                                    locationsLiveData.value = locationsList
                                }
                            }
                        }

                        override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                            Log.w(TAG, "Error getting location for address $address.", t)
                        }
                    })
            }
        }

        return locationsLiveData
    }
}