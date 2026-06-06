package com.example.aplapollo.model.Pickling

data class PicklingJobInProgressResponse(

    val picklingTranId: Int = 0,

    val barcode: String? = null,

    val thickness: Double = 0.0,

    val grade: String? = null,

    val width: Double = 0.0,

    val status: String? = null,

    val createdDateTime: String? = null,

    val picklingJobDetailsResponses:
    List<PicklingJobDetailsResponse>? = null
)

data class PicklingJobDetailsResponse(
        val weight: Double? = null,
    val barcode: String? = null
)