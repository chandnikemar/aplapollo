package com.example.aplapollo.model.CRM

data class CRMPlanResponse(

    val crmPlanId: Int,

    val crmPlanNo: String,

    val tenantCode: String,

    val materialCode: String,

    val grade: String,

    val thickness: Double,

    val width: Double,

    val quantity: Double,

    val uoM: String,

    val status: String,

    val totalRecord: Int,

   val crmPlanDetail: List<CRMPlanDetail>,

    val isActive: Boolean,

    val createdBy: String,

    val createdDate: String,

    val modifiedBy: String?,

    val modifiedDate: String,

    val tenantGroupCode: String?
)

data class CRMPlanDetail(

    val crmPlanDtlId: Int,

    val crmPlanId: Int,

    val requiredCoilWidth: Double,

    val isActive: Boolean
)

