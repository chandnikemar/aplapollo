package com.example.aplapollo.model.GP



data class GpOngoingJobsResponse(

    val galvanizingTranId: Int = 0,

    val barcode: String? = null,

    val thickness: Double = 0.0,

    val grade: String? = null,

    val width: Double = 0.0,

    val status: String? = null,

    val createdDateTime: String? = null,

    val galvanizingJobDetailsResponse:
    List<GalvanizingJobDetailsResponse>? = null
)

data class GalvanizingJobDetailsResponse(
    val weight: Double? = null,
    val barcode: String? = null
)