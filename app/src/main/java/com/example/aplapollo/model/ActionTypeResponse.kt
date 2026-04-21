package com.example.aplapollo.model

data class ActionTypeResponse(
    val actionTypeId: Int,
    val actionType: String,
    val displayName: String,
    val description: String,
    val totalRecord: Int,
    val isActive: Boolean,
    val createdBy: String,
    val createdDate: String,
    val modifiedBy: String?,
    val modifiedDate: String?,
    val tenantCode: String?,
    val tenantGroupCode: String?
)