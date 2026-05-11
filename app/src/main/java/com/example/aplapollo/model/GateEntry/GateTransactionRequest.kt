package com.example.aplapollo.model.GateEntry

data class GateTransactionRequest(
    val GateTransactionId: Int = 0,
    val LocationId: Int? = null,
    val GateEntryType: String,
    val VehicleNumber: String,
    val TransporterName: String,
    val TransporterNo: String,
    val LRNumber: String,
    val GateEntryNo: String? = null,
)