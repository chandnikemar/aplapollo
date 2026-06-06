    package com.example.aplapollo.api



    import HrSlittingStatusResponse
    import com.example.aplapollo.helper.Constants.ADD_Supplier
    import com.example.aplapollo.helper.Constants.DeleteChildPickling
    import com.example.aplapollo.helper.Constants.GATE_Transaction
    import com.example.aplapollo.helper.Constants.GET_ADDCRM_Child
    import com.example.aplapollo.helper.Constants.GET_ALL_GRADE
    import com.example.aplapollo.helper.Constants.GET_ALL_GSM
    import com.example.aplapollo.helper.Constants.GET_ALL_Transporter_List
    import com.example.aplapollo.helper.Constants.GET_ActionType
    import com.example.aplapollo.helper.Constants.GET_BomInputCode
    import com.example.aplapollo.helper.Constants.GET_CRM_BY_Id
    import com.example.aplapollo.helper.Constants.GET_CRM_PlanID
    import com.example.aplapollo.helper.Constants.GET_CRM_PlannedList
    import com.example.aplapollo.helper.Constants.GET_CoilSlit
    import com.example.aplapollo.helper.Constants.GET_ConfigsKey
    import com.example.aplapollo.helper.Constants.GET_CrmDelete
    import com.example.aplapollo.helper.Constants.GET_DELETE_TRANSACTION
    import com.example.aplapollo.helper.Constants.GET_Delete_Child
    import com.example.aplapollo.helper.Constants.GET_GATE_Transaction_Edit
    import com.example.aplapollo.helper.Constants.GET_GATE_Transaction_List
    import com.example.aplapollo.helper.Constants.GET_GPDelete
    import com.example.aplapollo.helper.Constants.GET_GPDelete_Child
    import com.example.aplapollo.helper.Constants.GET_GPDetail_Id
    import com.example.aplapollo.helper.Constants.GET_GpAdd_Child
    import com.example.aplapollo.helper.Constants.GET_GpBOmComponent
    import com.example.aplapollo.helper.Constants.GET_Initiate_GP
    import com.example.aplapollo.helper.Constants.GET_Location
    import com.example.aplapollo.helper.Constants.GET_Ongoing_GP
    import com.example.aplapollo.helper.Constants.GET_Ongoing_Pickl_Jobs
    import com.example.aplapollo.helper.Constants.GET_Pick_AddChild
    import com.example.aplapollo.helper.Constants.GET_PicklingDelete
    import com.example.aplapollo.helper.Constants.GET_Pickling_By_Id
    import com.example.aplapollo.helper.Constants.GET_Pickling_Product_By_Barcode
    import com.example.aplapollo.helper.Constants.GET_PrintLabel
    import com.example.aplapollo.helper.Constants.GET_ProcessMachine
    import com.example.aplapollo.helper.Constants.GET_QCHistory
    import com.example.aplapollo.helper.Constants.GET_RegisterConfig
    import com.example.aplapollo.helper.Constants.GET_SlitCoil
    import com.example.aplapollo.helper.Constants.GET_Slitting_Add
    import com.example.aplapollo.helper.Constants.GET_Slitting_Delete
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
    import com.example.aplapollo.model.ActionTypeResponse
    import com.example.aplapollo.model.ApiCommonResponse
    import com.example.aplapollo.model.BomResponse
    import com.example.aplapollo.model.CRM.CRMPlanResponse
    import com.example.aplapollo.model.CRM.CRMTransactionRequest
    import com.example.aplapollo.model.CRM.CRMTransactionResponse
    import com.example.aplapollo.model.CRM.OngoingCRMJobResponse
    import com.example.aplapollo.model.GP.BoMComponentResponse
    import com.example.aplapollo.model.GP.GalvanizingTransactionRequest
    import com.example.aplapollo.model.GP.GalvanizingTransactionResponse
    import com.example.aplapollo.model.GP.GpOngoingJobsResponse
    import com.example.aplapollo.model.GSMResponse
    import com.example.aplapollo.model.GateEntry.CoilSubmitRequest
    import com.example.aplapollo.model.GateEntry.GateEntryResponse
    import com.example.aplapollo.model.GateEntry.GateTransactionRequest
    import com.example.aplapollo.model.GateEntry.GateTransactionResponse
    import com.example.aplapollo.model.GateEntry.TransporterResponse
    import com.example.aplapollo.model.GradeResponse
    import com.example.aplapollo.model.LocationPaginationRequest
    import com.example.aplapollo.model.LocationResponse
    import com.example.aplapollo.model.Pickling.PicklingJobInProgressResponse
    import com.example.aplapollo.model.Pickling.PicklingTransactionResponse
    import com.example.aplapollo.model.Pickling.ProcessPicklingRequest
    import com.example.aplapollo.model.PrintLabelBarcodeRequest
    import com.example.aplapollo.model.ProcessMachineMappingResponse
    import com.example.aplapollo.model.QualityCheck.PrintLabelRequest
    import com.example.aplapollo.model.QualityCheck.PrintZplResponse
    import com.example.aplapollo.model.QualityCheck.QCFetchRequest
    import com.example.aplapollo.model.QualityCheck.QCFetchResponse
    import com.example.aplapollo.model.QualityCheck.QCStatusSubmissionRequest
    import com.example.aplapollo.model.QualityCheck.QCStatusSubmissionResponse
    import com.example.aplapollo.model.QualityCheck.QcTransactionResponse
    import com.example.aplapollo.model.Slitting.ApiResponse
    import com.example.aplapollo.model.Slitting.ApplicationConfigMaster
    import com.example.aplapollo.model.Slitting.CoilSplitRequest
    import com.example.aplapollo.model.Slitting.HrSlittingCompleteRequest
    import com.example.aplapollo.model.Slitting.HrSlittingItemAgainstPlanRequest
    import com.example.aplapollo.model.Slitting.HrSlittingItemAgainstPlanResponse
    import com.example.aplapollo.model.Slitting.HrSlittingPlanResponse
    import com.example.aplapollo.model.Slitting.HrSlittingscanReponse
    import com.example.aplapollo.model.Slitting.InitiateSlittingRequest
    import com.example.aplapollo.model.Slitting.InitiateSlittingResponse
    import com.example.aplapollo.model.Slitting.InitiateSlittingWithoutPlanRequest
    import com.example.aplapollo.model.Slitting.OngoingJobResponse
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

        @GET(GET_ALL_GRADE)
        suspend fun getGrades(): Response<List<GradeResponse>>

        @GET(GET_ALL_GSM)
        suspend fun getGSMList(): Response<List<GSMResponse>>
        @GET(GET_QCHistory)
        suspend fun getAllQcTransaction(): Response<List<QcTransactionResponse>>

        @GET(Get_Slitting_planById)
        suspend fun getHrSlittingPlanById(
            @Query("hrSlittingPlanId") hrSlittingPlanId: Int?): Response<HrSlittingPlanResponse>

        @GET(Get_HRSlitting_Scan)
        suspend fun getScanByBarcode(
            @Query("barcode") barcode: String?,
            @Query("hrSlittingPlanId")hrSlittingPlanId:Int?
        ): Response<HrSlittingscanReponse>

        @POST(Get_AllItemAgainstPlan)
        suspend fun getAllItemAgainstPlan(
            @Body request: HrSlittingItemAgainstPlanRequest
        ): Response<List<HrSlittingItemAgainstPlanResponse>>
        @POST(GET_SlitCoil)
        suspend fun getSlitCoil(
            @Body request: CoilSplitRequest
        ): Response<ApiCommonResponse>

        @POST(Get_SubmitSlit_From_Plan)
        suspend fun initiateSlitting(
            @Body request: InitiateSlittingRequest
        ): Response<InitiateSlittingResponse>

        @GET(Get_OnGoing)
        suspend fun getOngoingJobs(

            @Query("locationId") locationId: Int,
            @Query("process") process: String
        ): Response<List<OngoingJobResponse>>
        @POST(GET_CoilSlit)
        suspend fun coilSplit(
            @Body request: CoilSplitRequest
        ): Response<ApiCommonResponse>

        @GET(GET_DELETE_TRANSACTION)
        suspend fun getSlittingTranDelete(
            @Query("HRSlittingTranId") HRSlittingTranId : Int
        ): Response<ApiCommonResponse>
        @GET(GET_PicklingDelete)
        suspend fun getPicklingDelete(
            @Query("picklingTranId") picklingTranId : Int
        ): Response<ApiCommonResponse>
        @GET(GET_CrmDelete)
        suspend fun getCRMDelete(
            @Query("crmTranId") crmTranId : Int
        ): Response<ApiCommonResponse>
        @GET(GET_GPDelete)
        suspend fun getGpDelete(
            @Query("galvanizingTranId") galvanizingTranId : Int
        ): Response<ApiCommonResponse>

        @GET(GET_Slitting_Add)
        suspend fun getSlittingAddChild(
            @Query("hrSlittingTransId") hrSlittingTransId: Int,
            @Query("tenantCode") tenantCode: String
        ): Response<ApiCommonResponse>
        @GET(GET_Slitting_Delete)
        suspend fun getSlittingDeleteChild(
            @Query("hrSlittingTransDetailsId") hrSlittingTransDetailsId: Int
        ): Response<ApiCommonResponse>

        @GET(Get_Hr_Slitting_Detail)
        suspend fun getHrSlittingDetailsById(
            @Query("hrSlittingTranId") hrSlittingTranId: Int
        ): Response<HrSlittingStatusResponse>
        @GET(GET_ConfigsKey)
        suspend fun getConfigKey(
            @Query("key") key : String
        ): Response<ApplicationConfigMaster>
        @POST(GET_RegisterConfig)
        suspend fun getRegisterConfig(
            @Body request: ApplicationConfigMaster
        ): Response<ApiCommonResponse>

        @POST(GET_Location)
        suspend fun getLocationsWithPagination(
            @Body request: LocationPaginationRequest
        ): Response<List<LocationResponse>>
        @POST(Get_Complete_Hr_Slitting)
        suspend fun completeHRSlitting(
            @Body request: HrSlittingCompleteRequest
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
           @Query("locationId") locationId: Int,
           @Query("process")process: String
                   ): Response<List<PicklingJobInProgressResponse>>
        @GET(GET_Ongoing_GP)
        suspend fun getOngoingGpJobs(
            @Query("locationId") locationId: Int,
            @Query("process")process: String
        ): Response<List<GpOngoingJobsResponse>>
        @GET(GET_Pickling_Product_By_Barcode)
        suspend fun getPicklingBarcodeData(
            @Query("barcode") code: String?
        ): Response<ApiResponse<StockBarcodeWithoutplanResponse>>
        @POST(POST_Process_Pickling)
        suspend fun processPickling(
           @Body request: ProcessPicklingRequest
        ): Response<ApiCommonResponse>
        @POST(GET_Initiate_GP)
        suspend fun initiateGP(
            @Body request: GalvanizingTransactionRequest
        ): Response<ApiCommonResponse>
        @GET(GET_GPDetail_Id)
        suspend fun getGpDetailById(
            @Query("galvanizingTranId") galvanizingTranId: Int
        ): Response<GalvanizingTransactionResponse>

        @GET(GET_Pickling_By_Id)
        suspend fun getPicklingTransaction(
            @Query("picklingTranId") picklingTranId: Int
        ): Response<PicklingTransactionResponse>

        @GET(GET_Pick_AddChild)
        suspend fun getPicklingAddChild(
            @Query("picklingTransId") picklingTranId: Int,
            @Query("tenantCode") tenantCode: String
        ): Response<ApiCommonResponse>
        @GET(DeleteChildPickling)
        suspend fun getPicklingDeleteChild(
            @Query("picklingTransDetailsId") picklingTransDetailsId: Int
        ): Response<ApiCommonResponse>

        //CRM

        @GET(GET_ADDCRM_Child)
        suspend fun getCRMAddChild(
            @Query("crmTransId") crmTransId: Int,
            @Query("tenantCode") tenantCode: String
        ): Response<ApiCommonResponse>
        @GET(GET_Delete_Child)
        suspend fun getCRMDeleteChild(
            @Query("crmTransDetailsId") crmTransDetailsId: Int,

        ): Response<ApiCommonResponse>
        @GET(GET_GpAdd_Child)
        suspend fun getGpAddChild(
            @Query("galvanizingTranId") galvanizingTranId: Int,
            @Query("tenantCode") tenantCode: String
        ): Response<ApiCommonResponse>
        @GET(GET_GPDelete_Child)
        suspend fun getGpDeleteChild(
            @Query("galvanizingTransactionDetailsId") galvanizingTransactionDetailsId: Int,

            ): Response<ApiCommonResponse>
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
            @Query("locationId") locationId: Int,
            @Query("process")process: String

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

        @POST(GATE_Transaction)
        suspend fun gateTransactionEntry(
            @Body request: GateTransactionRequest
        ): Response<GateTransactionResponse>
        @POST(ADD_Supplier)
        suspend fun saveGateTransactionItem(
            @Body request: CoilSubmitRequest
        ): Response<ApiCommonResponse>

        @GET(GET_ActionType)
        suspend fun getActionType(
        ): Response<List<ActionTypeResponse>>

        @GET(GET_ProcessMachine)
        suspend fun proccesMachinById(
            @Query("actionTypeId") actionTypeId: Int?
        ): Response<List<ProcessMachineMappingResponse>>
        @GET(GET_BomInputCode)
        suspend fun getBomByInputCode(
            @Query("inputCode") inputCode: String
        ): Response<List<BomResponse>>


        @GET(GET_GpBOmComponent)
        suspend fun getGpBomByInputCode(
            @Query("inputMaterial") inputMaterial: String
        ): Response<List<BoMComponentResponse>>
        @GET(GET_GATE_Transaction_List)
        suspend fun getGateTransactionList(
        ):Response<List<GateEntryResponse>>
        @GET(GET_ALL_Transporter_List)
        suspend fun getTransporterList(
        ):Response<List<TransporterResponse>>
        @GET(GET_GATE_Transaction_Edit)
        suspend fun getGateTransactionUpdate(
            @Query("gateTransactionId") gateTransactionId: Int
        ):Response<List<GateEntryResponse>>

    }