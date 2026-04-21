package com.example.aplapollo.model

data class BoMMasterResponse(
    val boMId: Int,
    val boMNumber: String,
    val inputMaterial: String,
    var inputWeight:Double,
    val boMOutput: List<BoMOutputResponse>
)

data class BoMOutputResponse(
    val boMOutputId: Int,
    val boMMasterId: Int,
    val outputMaterial: String,
    var weight:Double,

    val boMComponent: List<BoMComponentResponse>
)

data class BoMComponentResponse(
    val boMComponentId: Int,
    val boMOutputId: Int,
        val componentCode: String,
    var weight:Double,
    var isSelected: Boolean = false
)