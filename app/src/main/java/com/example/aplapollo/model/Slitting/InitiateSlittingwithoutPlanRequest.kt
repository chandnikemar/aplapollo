    package com.example.aplapollo.model.Slitting

    data class InitiateSlittingWithoutPlanRequest(
        val HRSlittingTranId: Int,
    val TenantCode: String,
    val HRSlittingPlanId: Int,
    val LocationId: Int,
    val SourceStockId: Int,
    val JobNumber: String? = null,
    val Barcode: String? = null,
    val IronLossWeight: Double? = null,
    val ScrapWeight: Double? = null,
    val CompletedBy: String? = null,
    val CompletedDate: String? = null, // ISO format date
    val Status: String, // Draft, Planned, InProgress, Completed
    val Remarks: String? = null,
        val IsPlanned:Boolean,
        val IsActive: Boolean,
    val hrSlittingTransactionDetail: List<HRSlittingTransactionDetailRequest> =
        emptyList()
    )

    data class HRSlittingTransactionDetailRequest(
        val HRSlittingTranDtlId: Int,
        val HRSlittingTranId: Int,
        val Width: Double? = null,
        val Barcode: String? = null,
        val WeighAfterSlitting: Double? = null,
        val WeightTakenBy: String? = null,
        val WeightLocationId: Int,
        val WeightDatetime: String? = null, // ISO format date
        val IsActive: Boolean,
        val Status: String
    )


