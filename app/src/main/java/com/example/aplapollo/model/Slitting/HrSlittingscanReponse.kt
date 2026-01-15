package com.example.aplapollo.model.Slitting

data class ScanData(
    val stockId: Int,
    val materialTypeId: Int,
    val materialType: String?,
    val materialCode: String?,
    val actionTypeId: Int,
    val actionType: String?,
    val supplierName: String,
    val supplierBatchNo: String,
    val transactionId: Int,
    val parentStockId: Int?,
    val grade: String,
    val barcode: String,
    val weight: Double,
    val thickness: Double,
    val length: Double,
    val width: Double,
    val remarks: String?,
    val lastActionDoneBy: String,
    val lastActionDoneDate: String,
    val isActive: Boolean,
    val status: String,
    val totalRecord: Int
)
data class HrSlittingscanReponse(
    val responseObject: ScanData?,
    val errorMessage: String?,
    val exception: String?,
    val responseMessage: String?,
    val statusCode: Int
)
