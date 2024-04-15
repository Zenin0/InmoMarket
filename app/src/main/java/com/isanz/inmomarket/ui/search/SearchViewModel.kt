package com.isanz.inmomarket.ui.search

import android.content.ContentValues.TAG
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
    private val retrofit = Retrofit.Builder().baseUrl("https://maps.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create()).build()

    private val geocodingService = retrofit.create(GeocodingService::class.java)

    private fun getAllAdresses(): LiveData<List<Pair<String, String>>> {
        val addressesLiveData = MutableLiveData<List<Pair<String, String>>>()
        val currentUserId = InmoMarket.getAuth().currentUser!!.uid

        db.collection("properties").get().addOnSuccessListener { result ->
            val addressesList = result.documents.mapNotNull { document ->
                val userId = document.getString("userId") // replace "userId" with the actual field name in your Firestore documents
                val location = document.getString("location") // replace "location" with the actual field name in your Firestore documents
                if (userId != currentUserId && location != null) Pair(document.id, location) else null
            }
            addressesLiveData.value = addressesList
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error getting documents.", exception)
        }

        return addressesLiveData
    }

    fun getLatAndLong(): LiveData<List<Triple<String, Double, Double>>> {
        val locationsLiveData = MutableLiveData<List<Triple<String, Double, Double>>>()
        val locationsList = Collections.synchronizedList(ArrayList<Triple<String, Double, Double>>())

        getAllAdresses().observeForever { addresses ->
            addresses?.forEach { (id, address) ->
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
                            Log.w(TAG, "Error getting location for address $address.", t)
                        }
                    })
            }
        }

        return locationsLiveData
    }
}