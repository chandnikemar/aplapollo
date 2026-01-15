package com.example.aplapollo.model.login

data class LoginResponse(
    val userName: String?,
    val firstName: String?,
    val lastName: String?,
    val roleName: String?,
    val email: String?,
    val mobileNumber: String?,
    val landingPageURL: String?,
    val isVerified: Boolean?,
    val jwtToken: String?,
    val refreshToken: String?,
    val tenantGroup: String?,
    val defaultTenantCode: String?,
    val bpCode: String?,
    val userAccess: List<Any>?,

    val userTenants: List<Any>?
)
