package com.isanz.inmomarket.ui.search

import com.isanz.inmomarket.utils.retrofit.GeocodingResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingService {
    @GET("maps/api/geocode/json")
    fun getLatLng(
        @Query("address") location: String,
        @Query("key") apiKey: String
    ): Call<GeocodingResponse>
}