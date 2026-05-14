package com.example.aplapollo.model.CRM

import com.example.aplapollo.model.BomComponent
import com.example.aplapollo.model.BomOutput

data class CRMTransactionResponse(
    val crmTranId: Int? = null,
    val crmPlanId: Int? = null,
    val locationId: Int? = null,
    val locationCode: String? = null,
    val locantionName: String? = null,   // ⚠️ Keeping same spelling as API
    val sourceStockId: Int? = null,
    val jobNumber: String? = null,
    val motherBarcode: String? = null,
    val motherCoilWeight: Double? = null,
    val materialCode: String? = null,
    val barcode: String? = null,
    val ironLossWeight: Double? = null,
    val scrapWeight: Double? = null,
    val weightAfterCRM: Double?? = null,
    val isCoilDivided: Boolean? = null,
    val dividedCRMTranId: Int? = null,
    val completedBy: String?= null,
    val completedDate: String? = null,  // Can convert to Date if needed
    val status: String? = null,
    val remarks: String? = null,
    val isActive: Boolean? = null,
    val createdBy: String? = null,
    val createdDate: String? = null,
    val modifiedBy: String? = null,
    val modifiedDate: String? = null,
    val tenantCode: String? = null,
    val tenantGroupCode: String? = null,
    var crmTransactionDetails: MutableList<CRMTransactionDetailResponse>? = null
)
data class CRMTransactionDetailResponse(
    val crmTransactionDetailsId: Int? = null,

    val crmPlanId: Int? = null,
    var barcode: String? = null,

    val width: Double? = null,

    var weightAfterCrm: Double? = null,

    val weightTakenBy: String? = null,

    val weightDateTime: String? = null,



    var motherBarcode: String? = null,
    var materialCode: String? = null,
    var components: MutableList<BomComponent>? = mutableListOf(),



    var selectedOutputMaterial: BomOutput? = null,
    var isExpanded: Boolean = false
)
