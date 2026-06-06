package com.example.aplapollo.model.CRM

data class CRMTransactionRequest(

    val crmTranId: Int? = null,

    val crmPlanId: Int? = null,

    val locationId: Int? = null,

    val sourceStockId: Int? = null,

    val weight: Double? = null,

    val desiredThickness: Double? = null,

    val jobNumber: String? = null,

    val inputBarcode: String? = null,

    val inputWeight: Double? = null,

    val materialCode: String? = null,

    val ironLossWeight: Double? = null,

    val scrapWeight: Double? = null,

    val isCoilDivided: Boolean? = null,

    val completedBy: String? = null,

    val completedDate: String? = null,

    val status: String? = null,

    val remarks: String? = null,

    val isPlanned: Boolean? = null,

    val process: String? = null,

    val machineName: String? = null,

    val tamper: String? = null,

    val grade: String? = null,

    val crmTransactionDetails:
    MutableList<CRMTransactionDetailRequest>? = null
)

data class CRMTransactionDetailRequest(

    val crmTransactionDetailsId: Int? = null,

    val barcode: String? = null,

    val materialCode: String? = null,

    val weightAfterCRM: Double? = null,

    val uoM: String? = null,

    val weightTakenBy: String? = null,

    val weightDateTime: String? = null,

    val crmComponent:
    MutableList<CRMComponentRequest>? = null
)

data class CRMComponentRequest(

    val MaterialCode: String? = null,

    val Weight: Double? = null,

    val Uom: String? = null
)