    package com.example.aplapollo.model.Slitting

data class OngoingSlittingJobResponse(
    val crmTranId:Int?,
    val hrSlittingTranId: Int?,
    val tenantCode: String?,
    val hrSlittingPlanId: Int?,
    val sourceStockId: Int?,
    val locationId: Int?,
    val jobNumber: String?,
    val ironLossWeight: Double?,
    val scrapWeight: Double?,
    val completedBy: String?,
    val completedDate: String?,
    val status: String?,
    val isActive: Boolean?,
    val remarks: String?,
    val stockTransaction: StockTransaction
)
data class StockTransaction(
    val stockId: Int,
    val materialTypeId: Int,
    val materialType: String?,
    val materialCode: String?,
    val actionTypeId: Int,
    val actionType: String?,
    val supplierName: String?,
    val supplierBatchNo: String?,
    val transactionId: Int,
    val parentStockId: Int?,
    val grade: String?,
    val barcode: String?,
    val weight: Double?,
    val thickness: Double?,
    val length: Double?,
    val width: Double?,
    val remarks: String?,
    val lastActionDoneBy: String?,
    val lastActionDoneDate: String?,
    val isActive: Boolean,
    val status: String?,
    val totalRecord: Int?
)
