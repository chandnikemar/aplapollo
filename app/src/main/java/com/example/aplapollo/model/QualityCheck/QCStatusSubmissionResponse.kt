package com.example.aplapollo.model.QualityCheck

data class QCStatusSubmissionResponse(
    val responseObject:String,
    val errorMessage: String?,
    val exception: String?,
    val responseMessage: String?,
    val statusCode: Int
)
