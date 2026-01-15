package com.example.aplapollo.model.QualityCheck

data class PrintLabelRequest(
    val ZplContent: String? = null,
    val SupplierName: String,
    val BarCode: String,
    val SupplierBatchNo: String,
    val MaterialCode: String,
    val Grade: String,
    val Thickness: Double,
    val Width: Double,
    val GRNNumber: String,
    val GRNDate: String,      // ISO format: yyyy-MM-dd'T'HH:mm:ss
    val NetWeight: Double,
    val CreatedBy: String,
    val CreatedDate: String   // ISO format
)
