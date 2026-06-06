package com.example.aplapollo.model.GP

data class GalvanizingTransactionRequest(

    val galvanizingTranId: Int? = null,

    val locationId: Int? = null,

    val sourceStockId: Int? = null,

    val jobNumber: String? = null,

    val inputBarcode: String? = null,

    val inputWeight: Double? = null,

    val ironLossWeight: Double? = null,

    val scrapWeight: Double? = null,

    val isDivided: Boolean? = null,

    val completedBy: String? = null,

    val completedDate: String? = null,

    val status: String? = null,

    val remarks: String? = null,
    val zincMaterialCode:String?=null,
    val zincWeight:Double?=null,

    val process: String? = null,

    val machineName: String? = null,

    val tamper: String? = null,

    val grade: String? = null,

    val gsm: Double? = null,

    val galvanizingTransactionDetails:
    MutableList<GalvanizingTransactionDetailRequest>? = null
)

data class GalvanizingTransactionDetailRequest(

    val galvanizingTransactionDetailsId: Int? = null,

    val barcode: String? = null,

    val materialCode: String? = null,

    val weightAfterGalvanizing: Double? = null,

    val uoM: String? = null,

    val weightTakenBy: String? = null,

    val weightDateTime: String? = null,

    val galvanizingComponent:
    MutableList<GalvanizingComponentRequest>? = null
)

data class GalvanizingComponentRequest(

    val materialCode: String? = null,

    val weight: Double? = null,

    val uoM: String? = null,

    val isZincComponent:Boolean?=false
)