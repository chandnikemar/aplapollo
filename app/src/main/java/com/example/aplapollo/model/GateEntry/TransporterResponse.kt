package com.example.aplapollo.model.GateEntry

data class TransporterResponse(
    val transporterId: Int,
    val transporterName: String,
    val transporterCode: String,
    val companyCode: String? = null,
    val address: String? = null,
    val mobileNumber: String? = null,
    val email: String? = null,
    val isBlackListed: Boolean = false,
    val totalCount: Int = 0,
    val totalPages: Int = 0,
    val isActive: Boolean = true,
    val createdBy: String? = null,
    val createdDate: String? = null,
    val modifiedBy: String? = null,
    val modifiedDate: String? = null,
    val tenantCode: String? = null,
    val tenantGroupCode: String? = null
)
data class TransporterItem(
    val transporterId: Int,
    val transporterName: String,
    val transporterCode: String
) {
    override fun toString(): String {
        return "$transporterCode - $transporterName"
    }
}