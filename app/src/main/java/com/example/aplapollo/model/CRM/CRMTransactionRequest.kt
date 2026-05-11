package com.example.aplapollo.model.CRM


data class CRMTransactionRequest    (

    val crmTranId: Int,

    val crmPlanId: Int,
    val tenantCode: String?,
    val locationId: Int,

    val sourceStockId: Int,
    val weight:Double?,
    val desiredThickness: Double?,   // decimal? → Double?

    val jobNumber: String,
    val inputBarcode:String?,
    val inputWeight:String?,

        val barcode: String?,
val materialCode:String?,
    val ironLossWeight: Double?,

    val scrapWeight: Double?,

    val weightAfterCRM: Double?,

    val isCoilDivided: Boolean,

    val dividedCRMTranId: Int?,

    val completedBy: String?,

    val completedDate: String?,      // DateTime? → String (recommended for API)

    val status: String,
    val remarks: String?,
    val isPlanned :Boolean,

    val process: String?,
    val machineName: String?,
    val tamper: String?,
    val grade: String?,
    val component: List<ComponentsRequest>?
)
data class ComponentsRequest(
    val MaterialCode: String,
    val Weight: Double

)
