package com.example.aplapollo.model.GateEntry

data class GateTransactionResponse(
    val responseObject: GateTransactionData?,
    val errorMessage: String?,
    val exception: String?,
    val responseMessage: String?,
    val statusCode: Int
)
data class GateTransactionData(
    val gateTransactionId: Int,
    val locationId: Int?,
    val gateEntryType: String,
    val vehicleNumber: String,
    val transporterName: String,
    val transporterNo: String,
    val lrNumber: String,
    val gateEntryNo: String,
    val isActive: Boolean,
    val createdBy: String,
    val createdDate: String,
    val modifiedBy: String?,
    val modifiedDate: String?,
    val tenantCode: String,
    val tenantGroupCode: String
)