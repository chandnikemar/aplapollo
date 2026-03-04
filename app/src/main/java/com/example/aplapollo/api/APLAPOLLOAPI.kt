    package com.example.aplapollo.api

    import com.example.aplapollo.helper.Constants.GET_CRM_BY_Id
    import com.example.aplapollo.helper.Constants.GET_CRM_PlanID
    import com.example.aplapollo.helper.Constants.GET_CRM_PlannedList
    import com.example.aplapollo.helper.Constants.GET_Location
    import com.example.aplapollo.helper.Constants.GET_Ongoing_Pickl_Jobs
    import com.example.aplapollo.helper.Constants.GET_Pickling_By_Id
    import com.example.aplapollo.helper.Constants.GET_Pickling_Product_By_Barcode
    import com.example.aplapollo.helper.Constants.GET_PrintLabel
    import com.example.aplapollo.helper.Constants.GET_going_CRMJob
    import com.example.aplapollo.helper.Constants.Get_AllItemAgainstPlan
    import com.example.aplapollo.helper.Constants.Get_CRM_Scan
    import com.example.aplapollo.helper.Constants.Get_Complete_Hr_Slitting
    import com.example.aplapollo.helper.Constants.Get_GRNData
    import com.example.aplapollo.helper.Constants.Get_HRSlitting_Scan
    import com.example.aplapollo.helper.Constants.Get_HR_SlittingPlannned_List
    import com.example.aplapollo.helper.Constants.Get_Hr_Slitting_Detail
    import com.example.aplapollo.helper.Constants.Get_OnGoing
    import com.example.aplapollo.helper.Constants.Get_Slitting_planById
    import com.example.aplapollo.helper.Constants.Get_Stock_BYBatchOr_Barcode
    import com.example.aplapollo.helper.Constants.Get_SubmitSlit_From_Plan
    import com.example.aplapollo.helper.Constants.LOGIN_URL
    import com.example.aplapollo.helper.Constants.POST_CRMWithout_Plan
    import com.example.aplapollo.helper.Constants.POST_InitiateSlitting_WithoutPlan
    import com.example.aplapollo.helper.Constants.POST_Process_Pickling
    import com.example.aplapollo.helper.Constants.PosT_ProcessCRM
    import com.example.aplapollo.helper.Constants.Print_PRN
    import com.example.aplapollo.helper.Constants.QC_StatusSubmission
    import com.example.aplapollo.model.ApiCommonResponse
    import com.example.aplapollo.model.CRM.CRMPlanResponse
    import com.example.aplapollo.model.CRM.CRMTransactionRequest
    import com.example.aplapollo.model.CRM.CRMTransactionResponse
    import com.example.aplapollo.model.CRM.OngoingCRMJobResponse
    import com.example.aplapollo.model.LocationPaginationRequest
    import com.example.aplapollo.model.LocationResponse
    import com.example.aplapollo.model.Pickling.PicklingJobInProgressResponse
    import com.example.aplapollo.model.Pickling.PicklingTransactionResponse
    import com.example.aplapollo.model.Pickling.ProcessPicklingRequest
    import com.example.aplapollo.model.PrintLabelBarcodeRequest
    import com.example.aplapollo.model.QualityCheck.PrintLabelRequest
    import com.example.aplapollo.model.QualityCheck.PrintZplResponse
    import com.example.aplapollo.model.QualityCheck.QCFetchRequest
    import com.example.aplapollo.model.QualityCheck.QCFetchResponse
    import com.example.aplapollo.model.QualityCheck.QCStatusSubmissionRequest
    import com.example.aplapollo.model.QualityCheck.QCStatusSubmissionResponse
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
    import com.example.aplapollo.model.login.LoginResponse
    import retrofit2.Response
    import retrofit2.http.Body
    import retrofit2.http.GET
    import retrofit2.http.POST
    import retrofit2.http.Query

    interface APLAPOLLOAPI {
          @POST(LOGIN_URL)
          suspend fun login(
            @Body
            loginRequest: LoginRequest
        ): Response<LoginResponse>
        @POST(Get_GRNData)
        suspend fun getQCFetch(
            @Body request: QCFetchRequest
        ): Response<QCFetchResponse>
        @POST(QC_StatusSubmission)
        suspend fun qcStatusSubmission(

            @Body request: QCStatusSubmissionRequest
        ): Response<QCStatusSubmissionResponse>
            @POST(Print_PRN)
            suspend fun qcPrintLabel(

                @Body request: PrintLabelRequest
            ): Response<PrintZplResponse>



    //Slitting
        @GET(Get_HR_SlittingPlannned_List)
        suspend fun getHrSlittingPlannedList(
        ): Response<List<HrSlittingPlanResponse>>


        @GET(Get_Slitting_planById)
        suspend fun getHrSlittingPlanById(
            @Query("hrSlittingPlanId") hrSlittingPlanId: Int?
        ): Response<HrSlittingPlanResponse>

        @GET(Get_HRSlitting_Scan)
        suspend fun getScanByBarcode(
            @Query("barcode") barcode: String?,
            @Query("hrSlittingPlanId")hrSlittingPlanId:Int?
        ): Response<HrSlittingscanReponse>

        @POST(Get_AllItemAgainstPlan)
        suspend fun getAllItemAgainstPlan(
            @Body request: HrSlittingItemAgainstPlanRequest
        ): Response<List<HrSlittingItemAgainstPlanResponse>>

        @POST(Get_SubmitSlit_From_Plan)
        suspend fun initiateSlitting(
            @Body request: InitiateSlittingRequest
        ): Response<InitiateSlittingResponse>

        @GET(Get_OnGoing)
        suspend fun getOngoingJobs(
            @Query("locationId") locationId: Int
        ): Response<List<OngoingSlittingJobResponse>>



        @GET(Get_Hr_Slitting_Detail)
        suspend fun getHrSlittingDetailsById(
            @Query("hrSlittingTranId") hrSlittingTranId: Int
        ): Response<HrSlittingDetailsResponse>

        @POST(GET_Location)
        suspend fun getLocationsWithPagination(
            @Body request: LocationPaginationRequest
        ): Response<List<LocationResponse>>
        @POST(Get_Complete_Hr_Slitting)
        suspend fun completeHRSlitting(
            @Body request: HrSlittingTransactionRequest
        ): Response<ApiCommonResponse>
        @GET(Get_Stock_BYBatchOr_Barcode)
        suspend fun getStockByBatchOrBarcode(
            @Query("code") code: String?
        ): Response<ApiResponse<StockBarcodeWithoutplanResponse>>
        @POST(POST_InitiateSlitting_WithoutPlan)
        suspend fun initiateSlittingWithoutPlan(
            @Body request: InitiateSlittingWithoutPlanRequest
        ): Response<InitiateSlittingResponse>
       //Pickling
             @GET(GET_Ongoing_Pickl_Jobs)
          suspend fun getOngoingPicklingJobs(
           @Query("locationId") locationId: Int
                   ): Response<List<PicklingJobInProgressResponse>>
        @GET(GET_Pickling_Product_By_Barcode)
        suspend fun getPicklingBarcodeData(
            @Query("barcode") code: String?
        ): Response<ApiResponse<StockBarcodeWithoutplanResponse>>
        @POST(POST_Process_Pickling)
        suspend fun processPickling(
           @Body request: ProcessPicklingRequest
        ): Response<ApiCommonResponse>
        @GET(GET_Pickling_By_Id)
        suspend fun getPicklingTransaction(
            @Query("picklingTranId") picklingTranId: Int
        ): Response<PicklingTransactionResponse>

        //CRM
        @GET(GET_CRM_PlannedList)
        suspend fun getCRMPlannedList(
        ): Response<List<CRMPlanResponse>>
        @GET(Get_CRM_Scan)
        suspend fun getCRMScanByBarcode(
            @Query("barcode") barcode: String?,
            @Query("crmPlanId")crmPlanId:Int?
        ): Response<HrSlittingscanReponse>
        @GET(GET_CRM_PlanID)
        suspend fun getCRMPlanById(
            @Query("crmPlanId") crmPlanId: Int?
        ): Response<CRMPlanResponse>
        @POST(PosT_ProcessCRM)
        suspend fun processCRM(
            @Body request: CRMTransactionRequest
        ): Response<ApiCommonResponse>
        @GET(GET_going_CRMJob)
        suspend fun getOngoingCRMJobs(
            @Query("locationId") locationId: Int

        ): Response<List<OngoingCRMJobResponse>>
        @GET(GET_CRM_BY_Id)
        suspend fun getCRMPlanTranById(
            @Query("crmTranId") crmTranId: Int?
        ): Response<CRMTransactionResponse>

        @POST(POST_CRMWithout_Plan)
        suspend fun initiateCRMWithoutPlan(
            @Body request: CRMTransactionRequest
        ): Response<CRMTransactionResponse>
        @POST(GET_PrintLabel)
        suspend fun printLabelBarcode(
            @Body request:List<PrintLabelBarcodeRequest>
        ): Response<ApiCommonResponse>
    }