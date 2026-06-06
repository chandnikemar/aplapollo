package com.example.aplapollo.model.GP

import com.example.aplapollo.model.BomComponent
import com.example.aplapollo.model.BomOutput

data class GalvanizingTransactionResponse(

    val galvanizingTranId: Int? = 0,

    val locationId: Int? = 0,

    val sourceStockId: Int? = 0,

    val jobNumber: String? = null,
val zincMaterialCode:String?=null,
    val zincWeight:Double   ?=null,
    val motherBarcode: String? = null,

    val motherCoilWeight: Double? = 0.0,
val motherCoilWeightWithZinc:Double?=0.0,
    val uoM: String? = null,

    val materialCode: String? = null,

    val ironLossWeight: Double? = 0.0,

    val gsm: Double? = 0.0,

    val scrapWeight: Double? = 0.0,

    val completedBy: String? = null,

    val completedDate: String? = null,

    val status: String? = null,

    val remarks: String? = null,

    val isDivided: Boolean? = false,

    val allowedScrapWeightKg: Double? = 0.0,

    val allowedToleranceWeightKg: Double? = 0.0,
val allowedOutputWeightInTons:Double?=0.0,
    val totalRecord: Int? = 0,

    var galvanizingTransactionDetails:
    List<GalvanizingTransactionDetailsResponse>? = null,
    var localId: Long = System.currentTimeMillis(),
    var isDefaultRow: Boolean = false

)
data class GalvanizingTransactionDetailsResponse(

    val galvanizingTransactionDetailsId: Int? = 0,

    val galvanizingTranId: Int? = 0,

    val barcode: String? = null,

    val width: Double? = 0.0,

    var weightAfterGalvanizing: Double? = 0.0,

    val weightTakenBy: String? = null,

    val weightDateTime: String? = null,



    var motherBarcode: String? = null,
    var materialCode: String? = null,
    var components: MutableList<BomComponent>? = mutableListOf(),



    var selectedOutputMaterial: BomOutput? = null,
    var isExpanded: Boolean = false
)