package com.example.aplapollo.model.QualityCheck

data class MaterialTypeResponse(
    val MaterialTypes: List<MaterialTypeItem>?
)

data class MaterialTypeItem(
    val materialTypeId: Int,
    val materialType: String,
    val materialCode: String,
    val description: String?,
    val isActive: Boolean
)
