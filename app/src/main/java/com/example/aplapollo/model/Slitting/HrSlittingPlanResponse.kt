package com.example.aplapollo.model.Slitting



data class HrSlittingPlanResponse(
    val hrSlittingPlanId: Int,
    val hrSlittingPlanNo: String,
    val tenantCode: String,
    val materialCode: String,
    val grade: String,
    val thickness: Double,
    val width: Double,
    val quantity: Double,
    val uom: String,
    val totalRecord: Int,
    val hrSlittingPlanDetail: List<HrSlittingPlanDetail>,
    val isActive: Boolean,
    val createdBy: String,
    val createdDate: String,
    val modifiedBy: String?,
    val modifiedDate: String?,
    val tenantGroupCode: String?
)
data class HrSlittingPlanDetail(
    val hrSlittingPlanDtlId: Int,
    val hrSlittingPlanId: Int,
    val requiredCoilWidth: Double,
    val isActive: Boolean
)



