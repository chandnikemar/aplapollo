package com.example.aplapollo.model.Slitting

data class CoilSplitRequest(
    val StockId: Int,
    val Weight: Double,
    val Width: Double? = null,
    val Remark: String? = null,
    val UserName: String,
    val TenantCode: String
)