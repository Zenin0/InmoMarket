package com.isanz.inmomarket.utils.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

fun interface GeocodingService {
    @GET("maps/api/geocode/json")
    fun getLatLng(
        @Query("address") location: String,
        @Query("key") apiKey: String
    ): Call<GeocodingResponse>
}