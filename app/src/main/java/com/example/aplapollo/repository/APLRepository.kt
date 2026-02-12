package com.example.aplapollo.repository

import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.model.ApiCommonResponse
import com.example.aplapollo.model.LocationPaginationRequest
import com.example.aplapollo.model.LocationResponse
import com.example.aplapollo.model.Pickling.PicklingJobInProgressResponse
import com.example.aplapollo.model.Pickling.PicklingTransactionResponse
import com.example.aplapollo.model.Pickling.ProcessPicklingRequest
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

class APLRepository(private val retrofitInstance: RetrofitInstance) {


    suspend fun login(
        loginRequest: LoginRequest
    ) =
        retrofitInstance
            .tgsApi()
            .login(loginRequest)



    suspend fun fetchQCData(
        request: QCFetchRequest
    ): Response<QCFetchResponse> =
        retrofitInstance
            .serviceApi()
            .getQCFetch(request)

    suspend fun submitQCStatus(
        request: QCStatusSubmissionRequest
    ) =
        retrofitInstance
            .serviceApi()
            .qcStatusSubmission(request)

    suspend fun printLabelQC(
        request: PrintLabelRequest
    ) =
        retrofitInstance
            .serviceApi()
            .qcPrintLabel(request)


    suspend fun getHrPlannedList(

    ): Response<List<HrSlittingPlanResponse>> =
        retrofitInstance
            .serviceApi()
            .getHrSlittingPlannedList()
    suspend fun getHrSlittingPlanById(

        hrSlittingPlanId: Int
    ): Response<HrSlittingPlanResponse> =
        retrofitInstance
            .serviceApi()
            .getHrSlittingPlanById(hrSlittingPlanId)

    suspend fun getHrSlittingScan(

        barcode: String,
        hrSlittingPlanId: Int
    ): Response<HrSlittingscanReponse> =
        retrofitInstance
            .serviceApi()
            .getScanByBarcode(barcode, hrSlittingPlanId)



    suspend fun getAllItemAgainstPlan(

        request: HrSlittingItemAgainstPlanRequest
    ): Response<List<HrSlittingItemAgainstPlanResponse>> =
        retrofitInstance
            .serviceApi()
            .getAllItemAgainstPlan(request)


suspend fun initiateHrSlitting(

    request: InitiateSlittingRequest
): Response<InitiateSlittingResponse> =
    retrofitInstance
        .serviceApi()
        .initiateSlitting(request)

    suspend fun getOngoingJobs(

    ): Response<List<OngoingSlittingJobResponse>> =
        retrofitInstance
            .serviceApi()
            .getOngoingJobs()

suspend fun getHrSlittingDetailsById(
    tranId: Int
): Response<HrSlittingDetailsResponse> =
    retrofitInstance
        .serviceApi()
        .getHrSlittingDetailsById(tranId)

    suspend fun completeHRSlitting(

        request: HrSlittingTransactionRequest
    ): Response<ApiCommonResponse> =
        retrofitInstance
            .serviceApi()
            .completeHRSlitting(request)

    suspend fun getStockByBatchOrBarcode(
        code: String?
    ): Response<ApiResponse<StockBarcodeWithoutplanResponse>> =
        retrofitInstance
            .serviceApi()
            .getStockByBatchOrBarcode(code)
    suspend fun initiateSlittingWithoutPlan(
        request: InitiateSlittingWithoutPlanRequest
    ): Response<InitiateSlittingResponse> =
        retrofitInstance
            .serviceApi()
            .initiateSlittingWithoutPlan(request)

    suspend fun getLocations(
        request: LocationPaginationRequest
    ): Response<List<LocationResponse>> =
        retrofitInstance
            .serviceApi()
            .getLocationsWithPagination(request)

    suspend fun getOngoingPicklingJobs(

    ): Response<List<PicklingJobInProgressResponse>> =
        retrofitInstance
            .serviceApi()
            .getOngoingPicklingJobs()

    suspend fun getStockBarcodePicklingdata(
        code: String?
    ): Response<ApiResponse<StockBarcodeWithoutplanResponse>> =
        retrofitInstance
            .serviceApi()
            .getPicklingBarcodeData(code)

    suspend fun processPickling(

        request: ProcessPicklingRequest
    ): Response<ApiCommonResponse> =
        retrofitInstance
            .serviceApi()
            .processPickling(request)
    suspend fun getPicklingTransactionById(
        picklingTranId: Int
    ): Response<PicklingTransactionResponse> {

        return retrofitInstance
            .serviceApi()
            .getPicklingTransaction(picklingTranId)
    }



}
