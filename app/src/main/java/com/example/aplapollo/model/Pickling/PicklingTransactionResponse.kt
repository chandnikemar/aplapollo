package com.example.aplapollo.model.Pickling

import com.example.aplapollo.model.BomComponent
import com.example.aplapollo.model.BomOutput


data class PicklingTransactionResponse(

    val picklingTranId: Int? = null,

    val locationId: Int? = null,

    val sourceStockId: Int? = null,

    val jobNumber: String? = null,

    var motherBarcode: String? = null,

    val motherCoilWeight: Double? = null,
    val uoM: String?=null,
    val materialCode: String? = null,

    val ironLossWeight: Double? = null,

    val scrapWeight: Double? = null,

    val completedBy: String? = null,

    val completedDate: String? = null,

    val status: String? = null,

    val remarks: String? = null,

    val isDivided: Boolean? = null,

    val totalRecord: Int? = null,

    var picklingTransactionDetails:
    List<PicklingTransactionDetailResponse>? = null,

    val isActive: Boolean? = null,

    val createdBy: String? = null,

    val createdDate: String? = null,

    val modifiedBy: String? = null,

    val modifiedDate: String? = null,

    val tenantCode: String? = null,

    val tenantGroupCode: String? = null,
    var localId: Long = System.currentTimeMillis(),
   var isDefaultRow: Boolean = false
)

data class PicklingTransactionDetailResponse(

    val picklingTransactionDetailsId: Int? = null,

    val picklingTranId: Int? = null,

    var barcode: String? = null,

    val width: Double? = null,

    var weightAfterPickling: Double? = null,

    val weightTakenBy: String? = null,

    val weightDateTime: String? = null,



    var motherBarcode: String? = null,
    var materialCode: String? = null,
    var components: MutableList<BomComponent>? = mutableListOf(),



    var selectedOutputMaterial: BomOutput? = null,
    var isExpanded: Boolean = false

)