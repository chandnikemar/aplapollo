package com.example.aplapollo.api

import com.example.aplapollo.helper.Constants.GET_BARCODEValueWITHPREFIX
import com.example.aplapollo.helper.Constants.Get_GRNData
import com.example.aplapollo.helper.Constants.Get_HR_SlittingPlannned_List
import com.example.aplapollo.helper.Constants.Get_MaterialTypePage
import com.example.aplapollo.helper.Constants.Get_Slitting_planById
import com.example.aplapollo.helper.Constants.LOGIN_URL
import com.example.aplapollo.helper.Constants.Print_PRN
import com.example.aplapollo.helper.Constants.QC_StatusSubmission
import com.example.aplapollo.helper.Constants.REFRESH_TOKEN_DATA
import com.example.aplapollo.model.QualityCheck.BarcodePrefixResponse
import com.example.aplapollo.model.QualityCheck.MaterialTypeRequest
import com.example.aplapollo.model.QualityCheck.MaterialTypeResponse
import com.example.aplapollo.model.QualityCheck.PrintLabelRequest
import com.example.aplapollo.model.QualityCheck.PrintZplResponse
import com.example.aplapollo.model.QualityCheck.QCFetchRequest
import com.example.aplapollo.model.QualityCheck.QCFetchResponse
import com.example.aplapollo.model.QualityCheck.QCStatusSubmissionRequest
import com.example.aplapollo.model.QualityCheck.QCStatusSubmissionResponse
import com.example.aplapollo.model.RefreshTokenResponse
import com.example.aplapollo.model.Slitting.HrSlittingPlanResponse
import com.example.aplapollo.model.login.LoginRequest
import com.example.aplapollo.model.login.LoginResponse
import retrofit2.Call
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
    @POST(REFRESH_TOKEN_DATA)
     fun refreshToken(
        @Body body: Map<String, String>
    ): Call<RefreshTokenResponse>

    @POST(Get_GRNData)
    suspend fun getQCFetch(

        @Body request: QCFetchRequest
    ): Response<QCFetchResponse>

    @GET(GET_BARCODEValueWITHPREFIX)
    suspend fun getBarcodeWithPrefix(

        @Query("tenantCode") tenantCode: String?
    ): Response<BarcodePrefixResponse>

    @POST(QC_StatusSubmission)
    suspend fun qcStatusSubmission(

        @Body request: QCStatusSubmissionRequest
    ): Response<QCStatusSubmissionResponse>

        @POST(Print_PRN)
        suspend fun qcPrintLabel(

            @Body request: PrintLabelRequest
        ): Response<PrintZplResponse>

    @POST(Get_MaterialTypePage)
    suspend fun getMaterialTypes(

        @Body request: MaterialTypeRequest
    ): Response<MaterialTypeResponse>

//Slitting
    @GET(Get_HR_SlittingPlannned_List)
    suspend fun getHrSlittingPlannedList(
    ): Response<List<HrSlittingPlanResponse>>
    @GET(Get_Slitting_planById)
    suspend fun getHrSlittingPlanById(
        @Query("hrSlittingPlanId") hrSlittingPlanId: Int?
    ): Response<HrSlittingPlanResponse>

}