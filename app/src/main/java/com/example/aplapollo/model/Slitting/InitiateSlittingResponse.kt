package com.example.aplapollo.model.Slitting

data class InitiateSlittingResponse(
    val responseObject: HrSlittingResponseObject?,
    val errorMessage: String?,
    val exception: String?,
    val responseMessage: String?,
    val statusCode: Int?
)
data class HrSlittingResponseObject(
    val hrSlittingTranId: Int?,
    val tenantCode: String?,
    val hrSlittingPlanId: Int?,
    val locationId: Int?,
    val sourceStockId: Int?,
    val jobNumber: String?,
    val barcode: String?,
    val ironLossWeight: Double?,
    val scrapWeight: Double?,
    val completedBy: String?,
    val completedDate: String?,
    val status: String?,
    val remarks: String?,
    val isActive: Boolean?,
    val createdBy: String?,
    val createdDate: String?,
    val modifiedBy: String?,
    val modifiedDate: String?,
    val tenantGroupCode: String?
)
