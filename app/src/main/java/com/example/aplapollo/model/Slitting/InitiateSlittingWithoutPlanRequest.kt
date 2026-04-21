package com.example.aplapollo.model.Slitting

data class InitiateSlittingWithoutPlanRequest(
    val Barcode: String?,
    val CompletedBy: String?,
    val CompletedDate: String?,
    val HRSlittingPlanId: Int,
    val HRSlittingTranId: Int,
    val LocationId: Int,
    val Remarks: String?,
    val SourceStockId: Int,
    val Status: String?,
    val TenantCode: String,
    val hrSlittingTransactionDetail: List<HRSlittingTransactionDetailRequest>
)
data class HRSlittingTransactionDetailRequest(
    val HRSlittingTranDtlId: Int,
    val HRSlittingTranId: Int,
    val IsActive: Boolean,
    val Status: String,
    val WeightLocationId: Int,
    val Width: Double
)