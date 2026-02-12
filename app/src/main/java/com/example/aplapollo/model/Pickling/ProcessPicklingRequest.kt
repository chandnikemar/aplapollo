package com.example.aplapollo.model.Pickling

data class ProcessPicklingRequest(

    val picklingTranId: Int,

    val tenantCode: String,

    val locationId: Int,

    val sourceStockId: Int?,

    val jobNumber: String,

    val barcode: String,

    val ironLossWeight: Double?,

    val scrapWeight: Double?,

    val weightAfterPickling: Double?,

    val completedBy: String,

    val completedDate: String?,   // ISO format

    val status: String,

    val remarks: String,

    val isDivided: Boolean
)
