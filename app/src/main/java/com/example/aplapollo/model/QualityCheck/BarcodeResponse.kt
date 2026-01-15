package com.example.aplapollo.model.QualityCheck

data class BarcodePrefixResponse(
    val errorMessage: String?,
    val exception: String?,
    val responseMessage: String?,
    val statusCode: Int
)
