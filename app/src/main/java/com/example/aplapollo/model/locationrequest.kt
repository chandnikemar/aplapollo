package com.example.aplapollo.model

data class LocationPaginationRequest(
    val locationId: Int = 0,
    val locationName: String = "",
    val locationCode: String = "",
    val locationType: String? = null,
    val displayName: String? = null,
    val parentLocationId: Int? = null,
    val isActive: Boolean = true,
    val rowSize: Int = 0,
    val currentPage: Int = 0
)
