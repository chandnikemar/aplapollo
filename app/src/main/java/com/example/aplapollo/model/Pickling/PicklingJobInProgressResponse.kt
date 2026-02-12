package com.example.aplapollo.model.Pickling

data class PicklingJobInProgressResponse(
    val picklingTranId: Int,
    val barcode: String?,
    val thickness: Double,
    val grade: String?,
    val width: Double
)
