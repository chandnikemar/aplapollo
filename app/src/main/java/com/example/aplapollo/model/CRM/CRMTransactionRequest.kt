package com.example.aplapollo.model.CRM


data class CRMTransactionRequest(

    val crmTranId: Int,

    val crmPlanId: Int,
    val tenantCode: String?,
    val locationId: Int,

    val sourceStockId: Int,

    val desiredThickness: Double?,   // decimal? → Double?
val Weight:Double?,
    val jobNumber: String,

        val barcode: String?,

    val ironLossWeight: Double?,

    val scrapWeight: Double?,

    val weightAfterCRM: Double?,

    val isCoilDivided: Boolean,

    val dividedCRMTranId: Int?,

    val completedBy: String?,

    val completedDate: String?,      // DateTime? → String (recommended for API)

    val status: String,

    val remarks: String?,

    val isPlanned: Boolean?
)
