package com.example.aplapollo.model.CRM

data class OngoingCRMJobResponse(
    val crmTranId: Int,
    val barcode: String?,
    val thickness: Double,
    val grade: String?,
    val width: Double
)
