package com.example.aplapollo.model

data class LocationResponse(
    val locationId: Int,
    val locationName: String,
    val locationCode: String,
    val locationType: String,
    val displayName: String,
    val parentLocationId: Int?,
    val totalRecord: Int,
    val isActive: Boolean,
    val createdBy: String,
    val createdDate: String,
    val modifiedBy: String?,
    val modifiedDate: String?,
    val tenantCode: String,
    val tenantGroupCode: String?
)
