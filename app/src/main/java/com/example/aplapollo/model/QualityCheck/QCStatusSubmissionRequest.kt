package com.example.aplapollo.model.QualityCheck

data class QCStatusSubmissionRequest(
    val qcId: Int = 0,
    val tenantCode: String = "",
    val materialTypeId: Int = 0,
    val materialCode: String,
    val barcode: String,
    val supplierName: String,
    val supplierBatchNo: String,
    val grade: String,
    val netWeight: Double,
    val thickness: Double,
    val length: Double,
    val width: Double,
    val grnNo: String,
    val grnDate: String,
    val status: String,
    val remarks: String,
    val CreatedBy: String
)
