package com.isanz.inmomarket.utils.entities

import com.google.firebase.firestore.Exclude

data class Property(
    @Exclude var id: String? = null,
    var tittle: String = "",
    var description: String = "",
    var location: String = "",
    var userId: String = "",
    var listImagesUri: List<String> = listOf(),
    var extras: Map<String, Int> = mapOf(),
    val price: Double = 0.0,
    val squareMeters: Double = 0.0,
    val favorites: List<String> = listOf()
) {
    override fun toString(): String {
        return "Property(id=$id, tittle='$tittle', description='$description', location='$location', userId='$userId', listImagesUri=$listImagesUri, extras=$extras, price=$price)"
    }
}