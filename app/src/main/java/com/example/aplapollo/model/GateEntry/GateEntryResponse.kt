package com.example.aplapollo.model.GateEntry

data class GateEntryResponse(
    val gateTransactionId: Int,
    val tenantCode: String,
    val locationId: Int?,
    val gateEntryType: String,
    val vehicleNumber: String,
    val transporterName: String,
    val transporterNo: String,
    val lrNumber: String,
    val gateEntryNo: String,
    val gateTransactionItem: List<GateTransactionItem>?
)

data class GateTransactionItem(
    val gateTransactionItemId: Int,
    val gateTransactionId: Int,
    val coilBatch: String?,
    val status: String?,
    val remarks: String?,
    val isActive: Boolean?
)