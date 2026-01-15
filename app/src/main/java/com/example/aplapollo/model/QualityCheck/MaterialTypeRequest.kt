package com.example.aplapollo.model.QualityCheck

data class MaterialTypeRequest(
    val MaterialTypeId: Int = 0,
    val MaterialType: String? = null,
    val MaterialCode: String? = null,
    val Description: String? = null,
    val IsActive: Boolean? = null,
    val RowSize: Int? = null,
    val CurrentPage: Int? = null
)
