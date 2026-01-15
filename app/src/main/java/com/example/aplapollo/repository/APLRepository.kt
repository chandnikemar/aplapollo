package com.example.aplapollo.repository

import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.model.QualityCheck.MaterialTypeRequest
import com.example.aplapollo.model.QualityCheck.PrintLabelRequest
import com.example.aplapollo.model.QualityCheck.QCFetchRequest
import com.example.aplapollo.model.QualityCheck.QCFetchResponse
import com.example.aplapollo.model.QualityCheck.QCStatusSubmissionRequest
import com.example.aplapollo.model.Slitting.HrSlittingPlanResponse
import com.example.aplapollo.model.login.LoginRequest
import retrofit2.Response

class APLRepository {

    // 🔐 LOGIN API (NO JWT, NO INTERCEPTOR)
    suspend fun login(
        baseUrl: String,
        loginRequest: LoginRequest
    ) =
        RetrofitInstance.loginApi(baseUrl)
            .login(loginRequest)


    // 🔐 QC FETCH (JWT auto attached, auto refresh)
    suspend fun fetchQCData(
        baseUrl: String,
        request: QCFetchRequest
    ): Response<QCFetchResponse> =
        RetrofitInstance.serviceApi(baseUrl)
            .getQCFetch(request)


    suspend fun getBarcodeWithPrefix(
        baseUrl: String,
        tenantCode: String
    ) =
        RetrofitInstance.serviceApi(baseUrl)
            .getBarcodeWithPrefix(tenantCode)


    suspend fun submitQCStatus(
        baseUrl: String,
        request: QCStatusSubmissionRequest
    ) =
        RetrofitInstance.serviceApi(baseUrl)
            .qcStatusSubmission(request)


    suspend fun getMaterialTypes(
        baseUrl: String,
        request: MaterialTypeRequest
    ) =
        RetrofitInstance.serviceApi(baseUrl)
            .getMaterialTypes(request)


    suspend fun printLabelQC(
        baseUrl: String,
        request: PrintLabelRequest
    ) =
        RetrofitInstance.serviceApi(baseUrl)
            .qcPrintLabel(request)


    suspend fun getHrPlannedList(
        baseUrl: String
    ): Response<List<HrSlittingPlanResponse>> =
        RetrofitInstance.serviceApi(baseUrl)
            .getHrSlittingPlannedList()
    suspend fun getHrSlittingPlanById(
        baseUrl: String,
        hrSlittingPlanId: Int
    ): Response<HrSlittingPlanResponse> =
        RetrofitInstance.serviceApi(baseUrl)
            .getHrSlittingPlanById(hrSlittingPlanId)

}
