package com.example.aplapollo.model

data class ApiCommonResponse(
    val responseObject: Any?,
    val errorMessage: String?,
    val exception: String?,
    val responseMessage: String?,
    val statusCode: Int?
)
