package com.example.aplapollo.model.Slitting

data class HrSlittingCompleteRequest(
    val HRSlittingTranId: Int,
    val TenantCode: String,
    val HRSlittingPlanId: Int,
    val LocationId: Int,
    val SourceStockId: Int,
    val Weight: Double,
    val JobNumber: String,
    val Barcode: String,
    val IronLossWeight: Double,
    val ScrapWeight: Double,
    val CompletedBy: String,
    val CompletedDate: String,
    val Status: String,
    val Remarks: String,
    val IsPlanned: Boolean,
    val Process: String,
    val MachineName: String,
    val Tamper: String,
    val Grade: String,
    val hrSlittingTransactionDetail: List<HrSlittingCompleteTransactionDetails>
)
data class HrSlittingCompleteTransactionDetails(
    val HRSlittingTranDtlId: Int,
    val HRSlittingTranId: Int,
    val Width: Double,
    val Barcode: String,
    val MaterialCode: String,
    val WeighAfterSlitting: Double,
    val WeightTakenBy: String,
    val WeightLocationId: Int,
    val WeightDatetime: String,
    val IsActive: Boolean,
    val Status: String,
    val Uom: String,
    val Component: List<ComponentRequest>
)
data class ComponentRequest(
    val MaterialCode: String,
    val MaterilDesc:String,
    val Weight: Double,
    val Uom: String,
)


