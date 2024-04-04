package com.isanz.inmomarket.utils.entities

data class Parcela(
    var id: String? = null,
    var tittle: String = "",
    var description: String = "",
    var location: String = "",
    var rooms: Int = 0,
    var baths: Int = 0,
    var userId: String = "",
    var listImagesUri: List<String> = listOf()
)