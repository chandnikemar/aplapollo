package com.example.aplapollo.model.Slitting

//data class OngoingJobResponse(
//    val hrSlittingTranId: Int?,
//    val tenantCode: String?,
//    val hrSlittingPlanId: Int?,
//    val sourceStockId: Int?,
//    val locationId: Int?,
//    val jobNumber: String?,
//    val status: String?,
//    val stockTransaction: StockTransactions?
//)
//data class StockTransactions(
//    val stockId: Int?,
//    val supplierName: String?,
//    val supplierBatchNo: String?,
//    val grade: String?,
//    val barcode: String?,
//    val weight: Double?,
//    val thickness: Double?,
//    val width: Double?
//)
data class OngoingJobResponse(

    val hrSlittingTranId: Int = 0,

    val barcode: String? = null,

    val thickness: Double = 0.0,

    val grade: String? = null,

    val width: Double = 0.0,

    val status: String? = null,

    val createdDateTime: String? = null,

    val hRSlittingJobDetailsResponse:
    List<HRSlittingJobDetailsResponse>? = null
)

data class HRSlittingJobDetailsResponse(

    val barcode: String? = null,
    val weight:Double?=0.0
)