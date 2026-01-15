package com.example.aplapollo.model.QualityCheck

data class QCFetchData(
    val supplierBatchNo: String?,
    val supplierName: String?,
    val netWeightKg: Double?,
    val materialCode: String?,
    val grade: String?,
    val thickness: Double?,
    val width: Double?,
    val length: Double?,
    val grnNumber: String?,
    val grnDate: String?
)
data class QCFetchResponse(
    val statusCode: Int,
    val errorMessage: String?,
    val responseMessage: String?,
    val responseObject: QCFetchData?
)


