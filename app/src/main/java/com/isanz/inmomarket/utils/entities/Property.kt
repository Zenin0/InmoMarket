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
    val squareMeters: Double = 0.0
)