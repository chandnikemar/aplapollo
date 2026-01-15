package com.example.aplapollo.repository

import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.model.ApiCommonResponse
import com.example.aplapollo.model.LocationPaginationRequest
import com.example.aplapollo.model.LocationResponse
import com.example.aplapollo.model.QualityCheck.PrintLabelRequest
import com.example.aplapollo.model.QualityCheck.QCFetchRequest
import com.example.aplapollo.model.QualityCheck.QCFetchResponse
import com.example.aplapollo.model.QualityCheck.QCStatusSubmissionRequest
import com.example.aplapollo.model.Slitting.ApiResponse
import com.example.aplapollo.model.Slitting.HrSlittingDetailsResponse
import com.example.aplapollo.model.Slitting.HrSlittingItemAgainstPlanRequest
import com.example.aplapollo.model.Slitting.HrSlittingItemAgainstPlanResponse
import com.example.aplapollo.model.Slitting.HrSlittingPlanResponse
import com.example.aplapollo.model.Slitting.HrSlittingTransactionRequest
import com.example.aplapollo.model.Slitting.HrSlittingscanReponse
import com.example.aplapollo.model.Slitting.InitiateSlittingRequest
import com.example.aplapollo.model.Slitting.InitiateSlittingResponse
import com.example.aplapollo.model.Slitting.InitiateSlittingWithoutPlanRequest
import com.example.aplapollo.model.Slitting.OngoingSlittingJobResponse
import com.example.aplapollo.model.Slitting.StockBarcodeWithoutplanResponse
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





    suspend fun submitQCStatus(
        baseUrl: String,
        request: QCStatusSubmissionRequest
    ) =
        RetrofitInstance.serviceApi(baseUrl)
            .qcStatusSubmission(request)





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

    suspend fun getHrSlittingScan(
        baseUrl: String,
        barcode: String,
        hrSlittingPlanId: Int
    ): Response<HrSlittingscanReponse> =
        RetrofitInstance.serviceApi(baseUrl)
            .getScanByBarcode(barcode,hrSlittingPlanId)


    suspend fun getAllItemAgainstPlan(
        baseUrl: String,
        request: HrSlittingItemAgainstPlanRequest
    ): Response<List<HrSlittingItemAgainstPlanResponse>> {

        return RetrofitInstance
            .serviceApi(baseUrl)
            .getAllItemAgainstPlan(request)
    }

    suspend fun initiateHrSlitting(
        baseUrl: String,
        request: InitiateSlittingRequest
    ): Response<InitiateSlittingResponse> =
        RetrofitInstance
            .serviceApi(baseUrl)
            .initiateSlitting(request)
    suspend fun getOngoingJobs(
            baseUrl: String
        ): Response<List<OngoingSlittingJobResponse>> {
            return RetrofitInstance
                .serviceApi(baseUrl)
                .getOngoingJobs()
        }

    suspend fun getHrSlittingDetailsById(
        baseUrl: String,
        tranId: Int
    ): Response<HrSlittingDetailsResponse> =
        RetrofitInstance.serviceApi(baseUrl)
            .getHrSlittingDetailsById(tranId)
    suspend fun getLocations(
        baseUrl: String,
        request: LocationPaginationRequest
    ): Response<List<LocationResponse>> =
        RetrofitInstance.serviceApi(baseUrl)
            .getLocationsWithPagination(request)

    suspend fun completeHRSlitting(
        baseUrl: String,
        request: HrSlittingTransactionRequest
    ): Response<ApiCommonResponse> =
        RetrofitInstance
            .serviceApi(baseUrl)
            .completeHRSlitting(request)

    suspend fun getStockByBatchOrBarcode(
        baseUrl: String,

        code: String?
    ): Response<ApiResponse<StockBarcodeWithoutplanResponse>> =
        RetrofitInstance
            .serviceApi(baseUrl)
            .getStockByBatchOrBarcode(
                 code
            )

    suspend fun initiateSlittingWithoutPlan(
        baseUrl: String,
        request: InitiateSlittingWithoutPlanRequest
    ): Response<InitiateSlittingResponse> =
        RetrofitInstance
            .serviceApi(baseUrl)
            .initiateSlittingWithoutPlan(request)

}
