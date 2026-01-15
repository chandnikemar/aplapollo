package com.example.aplapollo.model.Slitting

data class InitiateSlittingRequest(
    val HRSlittingTranId: Int,
    val TenantCode: String,
    val HRSlittingPlanId: Int,
    val LocationId: Int,
    val SourceStockId: Int,
    val IsActive:Boolean,
    val Status: String,
    val Remarks: String
)
