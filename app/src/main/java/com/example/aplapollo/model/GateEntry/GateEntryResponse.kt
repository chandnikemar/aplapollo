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
    val coilNo: String? = null,
    val weight: Double? = null,
    val materialCode: String? = null
)