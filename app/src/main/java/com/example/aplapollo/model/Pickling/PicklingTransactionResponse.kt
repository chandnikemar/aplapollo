package com.example.aplapollo.model.Pickling

data class PicklingTransactionResponse(
    val picklingTranId: Int,
    val tenantCode: String,
    val locationId: Int,
    val locationCode: String,
    val locationName: String,
    val sourceStockId: Int,
    val motherBarcode:String,
    val motherCoilWeight:Double,
    val jobNumber: String,
    val barcode: String?,
    val ironLossWeight: Double?,
    val scrapWeight: Double?,
    var weightAfterPickling: Double?, // After pickling
    val completedBy: String?,
    val completedDate: String?, // ISO Date String
    val status: String, // Queued, Completed
    val remarks: String?,
    val isDivided: Boolean = false,
    val totalRecord: Int
)