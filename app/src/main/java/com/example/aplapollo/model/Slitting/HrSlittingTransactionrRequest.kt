package com.example.aplapollo.model.Slitting

data class HrSlittingTransactionRequest(
    val hrSlittingTranId: Int,
    val tenantCode: String,
    val locationId: Int,
    val locationName: String?,
    val sourceStockId: Int,
    val jobNumber: String,
    val ironLossWeight: Double,
    val scrapWeight: Any,
    val completedBy: String?,
    val completedDate: String?,
    val status: String,
    val remarks: String,
    val totalRecord: Int,
    val hRSlittingTransactionDetail: List<HrSlittingTransactionDetail>
)
data class HrSlittingTransactionDetails(
    val hrSlittingTranDtlId: Int,
    val hrSlittingTranId: Int,
    val width: Double,
    val barcode: String,
    val weighAfterSlitting: Int,
    val weightTakenBy: String?,
    val weightLocationId: Int,
    val weightDatetime: String?,
    val status: String,
    val isActive: Boolean,
    val createdBy: String,
    val createdDate: String,
    val modifiedBy: String?,
    val modifiedDate: String?,
    val tenantCode: String?,
    val tenantGroupCode: String?
)
