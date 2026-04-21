package com.example.aplapollo.repository

import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.model.ActionTypeResponse
import com.example.aplapollo.model.ApiCommonResponse
import com.example.aplapollo.model.BoMMasterResponse
import com.example.aplapollo.model.CRM.CRMPlanResponse
import com.example.aplapollo.model.CRM.CRMTransactionRequest
import com.example.aplapollo.model.CRM.CRMTransactionResponse
import com.example.aplapollo.model.CRM.OngoingCRMJobResponse
import com.example.aplapollo.model.GateEntry.CoilSubmitRequest
import com.example.aplapollo.model.GateEntry.GateTransactionRequest
import com.example.aplapollo.model.GateEntry.GateTransactionResponse
import com.example.aplapollo.model.LocationPaginationRequest
import com.example.aplapollo.model.LocationResponse
import com.example.aplapollo.model.Pickling.PicklingJobInProgressResponse
import com.example.aplapollo.model.Pickling.PicklingTransactionResponse
import com.example.aplapollo.model.Pickling.ProcessPicklingRequest
import com.example.aplapollo.model.PrintLabelBarcodeRequest
import com.example.aplapollo.model.ProcessMachineMappingResponse
import com.example.aplapollo.model.QualityCheck.PrintLabelRequest
import com.example.aplapollo.model.QualityCheck.QCFetchRequest
import com.example.aplapollo.model.QualityCheck.QCFetchResponse
import com.example.aplapollo.model.QualityCheck.QCStatusSubmissionRequest
import com.example.aplapollo.model.Slitting.ApiResponse
import com.example.aplapollo.model.Slitting.HrSlittingDetailsResponse
import com.example.aplapollo.model.Slitting.HrSlittingItemAgainstPlanRequest
import com.example.aplapollo.model.Slitting.HrSlittingItemAgainstPlanResponse
import com.example.aplapollo.model.Slitting.HrSlittingPlanResponse
import com.example.aplapollo.model.Slitting.HrSlittingRequest
import com.example.aplapollo.model.Slitting.HrSlittingscanReponse
import com.example.aplapollo.model.Slitting.InitiateSlittingRequest
import com.example.aplapollo.model.Slitting.InitiateSlittingResponse
import com.example.aplapollo.model.Slitting.InitiateSlittingWithoutPlanRequest
import com.example.aplapollo.model.Slitting.OngoingJobResponse
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
        tenantCode: String,
        locationId: Int
    ): Response<List<OngoingJobResponse>> =
        retrofitInstance
            .serviceApi()
            .getOngoingJobs(tenantCode,locationId)

suspend fun getHrSlittingDetailsById(
    tranId: Int
): Response<HrSlittingDetailsResponse> =
    retrofitInstance
        .serviceApi()
        .getHrSlittingDetailsById(tranId)

    suspend fun completeHRSlitting(

        request: HrSlittingRequest
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
        locationId: Int

    ): Response<List<PicklingJobInProgressResponse>> =
        retrofitInstance
            .serviceApi()
            .getOngoingPicklingJobs(locationId)

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

    suspend fun getCRMPlannedList(

    ): Response<List<CRMPlanResponse>> =
        retrofitInstance
            .serviceApi()
            .getCRMPlannedList()

    suspend fun getCRMScan(

        barcode: String,
        crmPlanId: Int
    ): Response<HrSlittingscanReponse> =
        retrofitInstance
            .serviceApi()
            .getCRMScanByBarcode(barcode, crmPlanId )
    suspend fun getCrmPlanById(

        crmPlanId: Int
    ): Response<CRMPlanResponse> =
        retrofitInstance
            .serviceApi()
            .getCRMPlanById(crmPlanId)


    suspend fun processCRM(

        request: CRMTransactionRequest
    ): Response<ApiCommonResponse> =
        retrofitInstance
            .serviceApi()
            .processCRM(request)


    suspend fun getOngoingCRMJobs(
        locationId: Int
    ): Response<List<OngoingCRMJobResponse>> =
        retrofitInstance
            .serviceApi()
            .getOngoingCRMJobs(locationId)


    suspend fun getCrmPlanTranById(

        crmTranId: Int
    ): Response<CRMTransactionResponse> =
        retrofitInstance
            .serviceApi()
            .getCRMPlanTranById(crmTranId)


    suspend fun initiateCRMWithoutPlan(
        request: CRMTransactionRequest
    ): Response<CRMTransactionResponse> =
        retrofitInstance
            .serviceApi()
            .initiateCRMWithoutPlan(request)

    suspend fun printLabelBarcode(
        request: List<PrintLabelBarcodeRequest>
    ): Response<ApiCommonResponse> =
        retrofitInstance
            .serviceApi()
            .printLabelBarcode(request)

    suspend fun gateTransactionEntry(
        request: GateTransactionRequest
    ): Response<GateTransactionResponse> =
        retrofitInstance.serviceApi().gateTransactionEntry(request)

    suspend fun saveGateTransactionItem(
        request: CoilSubmitRequest
    ): Response<ApiCommonResponse> =
        retrofitInstance
            .serviceApi()
            .saveGateTransactionItem(request)

    suspend fun getActionTypeList(

    ): Response<List<ActionTypeResponse>> =
        retrofitInstance
            .serviceApi()
            .getActionType()
    suspend fun getProccesMachineById(

        actionTypeId: Int
    ): Response<List<ProcessMachineMappingResponse>> =
        retrofitInstance
            .serviceApi()
            .proccesMachinById(actionTypeId)
    suspend fun getBomInputCode(
    inputCode:String
):Response<List<BoMMasterResponse>> = retrofitInstance
    .serviceApi().getBomByInputCode(inputCode)


}
