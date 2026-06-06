package com.example.aplapollo.model.CRM



data class OngoingCRMJobResponse(

    val crmTranId: Int = 0,

    val barcode: String? = null,

    val thickness: Double = 0.0,

    val grade: String? = null,

    val width: Double = 0.0,

    val status: String? = null,

    val createdDateTime: String? = null,

    val crmJobDetailsResponse:
    List<CRMJobDetailsResponse>? = null
)

data class CRMJobDetailsResponse(
    val weight: Double? = null,
    val barcode: String? = null
)