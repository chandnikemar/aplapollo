package com.example.aplapollo.model.CRM

data class CRMTransactionResponse(
    val crmTranId: Int,
    val crmPlanId: Int,
    val locationId: Int,
    val locationCode: String?,
    val locantionName: String?,   // ⚠️ Keeping same spelling as API
    val sourceStockId: Int,
    val jobNumber: String,
    val motherBarcode: String,
    val motherCoilWeight: Double,
    val materialCode: String,
    val barcode: String,
    val ironLossWeight: Double?,
    val scrapWeight: Double?,
    val weightAfterCRM: Double?,
    val isCoilDivided: Boolean,
    val dividedCRMTranId: Int?,
    val completedBy: String?,
    val completedDate: String?,  // Can convert to Date if needed
    val status: String,
    val remarks: String?,
    val isActive: Boolean,
    val createdBy: String,
    val createdDate: String,
    val modifiedBy: String?,
    val modifiedDate: String?,
    val tenantCode: String,
    val tenantGroupCode: String?
)
