package com.example.aplapollo.model

data class ProcessMachineMappingResponse(
    val processMachineMappingId: Int,
    val actionTypeId: Int,
    val actionType: String,
    val machineId: Int,
    val machineName: String,
    val isActive: Boolean,
    val createdBy: String,
    val createdDate: String,
    val modifiedBy: String?,
    val modifiedDate: String?,
    val tenantCode: String?,
    val tenantGroupCode: String?
)