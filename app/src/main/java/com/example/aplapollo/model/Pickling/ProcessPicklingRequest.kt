package com.example.aplapollo.model.Pickling

import com.example.aplapollo.model.BomComponent
import com.example.aplapollo.model.BomOutput

data class ProcessPicklingRequest   (

    val picklingTranId: Int? = null,
    val tenantCode: String?,
    val locationId: Int? = null,
    val sourceStockId: Int? = null,
    val jobNumber: String? = null,
    val status: String? = null,
    val remarks: String? = null,
    val isDivided: Boolean? = null,
    val IsActive: Boolean? = null,

    val inputBarcode: String? = null,
    val inputWeight: Double? = null,
    val ironLossWeight: Double? = null,
    val scrapWeight: Double? = null,
    val completedBy: String? = null,
    val completedDate: String? = null,
    val process: String? = null,
    val machineName: String? = null,
    val tamper: String? = null,
    val grade: String? = null,

    val PicklingTransactionDetails:
    List<PicklingTransactionDetail>? = null
)

data class PicklingTransactionDetail(

    val picklingTransactionDetailsId: Int? = null,
    val barcode: String? = null,
    val materialCode: String? = null,
    val width: Int? = null,
    val weightAfterPickling: Double? = null,
    val uoM: String? = null,
    val weightTakenBy: String? = null,
    val weightDateTime: String? = null,

    val picklingComponent:
    List<PicklingComponent>? = null
)

data class PicklingComponent(

    val materialCode: String? = null,

    val weight: Double? = null,
    val uoM: String? = null
)
data class PicklingJobUI(
    var picklingTranId: Int = 0,
    var barcode: String = "",
    var materialCode: String = "",
    var weightAfterPickling: Double = 0.0,

    var output: BomOutput? = null,
    var components: MutableList<BomComponent> = mutableListOf()
)
//data class ProcessPicklingRequest(
//    val PicklingTranId: Int,
//    val TenantCode: String?,
//    val LocationId: Int,
//    val SourceStockId: Int?,
//    val JobNumber: String?,
//    val InputBarcode: String?,
//    val InputWeight: Double?,
//    val Barcode: String?,
//    val MaterialCode: String?,
//    val IronLossWeight: Double?,
//    val ScrapWeight: Double?,
//    val WeightAfterPickling: Double?,
//    val CompletedBy: String?,
//    val CompletedDate: String?,
//    val Status: String?,
//    val Remarks: String?,
//    val IsDivided: Boolean,
//    val Process: String?,
//    val MachineName: String?,
//    val Tamper: String?,
//    val Grade: String?,
//    val Component: List<ComponentsRequest>?
//)
//data class ComponentsRequest(
//    val MaterialCode: String,
//    val Weight: Double
//)