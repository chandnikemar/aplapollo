package com.example.aplapollo.model.QualityCheck

data class QcTransactionResponse(
    val qcId: Int,
    val tenantCode: String?,
    val materialTypeId: Int?,
    val materialCode: String?,
    val barcode: String?,
    val CoilBatchNumber:String?,
    val supplierName: String?,
    val supplierBatchNo: String?,
    val grade: String?,
    val netWeight: Double?,
    val thickness: Double?,
    val length: Double?,
    val width: Double?,
    val grnNo: String?,
    val grnDate: String?,
    val status: String?,
    val remarks: String?,
    val totalRecord: Int?,
    val isActive: Boolean?,
    val createdBy: String?,
    val createdDate: String?,
    val modifiedBy: String?,
    val modifiedDate: String?,
    val tenantGroupCode: String?
)