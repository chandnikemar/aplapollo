package com.example.aplapollo.viewmodel.crm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.ApiCommonResponse
import com.example.aplapollo.model.CRM.CRMPlanResponse
import com.example.aplapollo.model.CRM.CRMTransactionRequest
import com.example.aplapollo.model.CRM.CRMTransactionResponse
import com.example.aplapollo.model.CRM.OngoingCRMJobResponse
import com.example.aplapollo.model.Slitting.HrSlittingscanReponse
import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class CRMViewModel(
    application: Application,
    private val aplRepository: APLRepository
): AndroidViewModel(application)  {
    val crmScanLiveData: MutableLiveData<Resource<HrSlittingscanReponse>> =
        MutableLiveData()

    val crmPlanMutableLiveData: MutableLiveData<Resource<List<CRMPlanResponse>>> =
        MutableLiveData()

    val CrmPlanDetailLiveData: MutableLiveData<Resource<CRMPlanResponse>> =
        MutableLiveData()
    val CrmPlanTranDetailLiveData: MutableLiveData<Resource<CRMTransactionResponse>> =
        MutableLiveData()

    val ProcessCRMLiveData: MutableLiveData<Resource<ApiCommonResponse>> =
        MutableLiveData()
    val ongoingJobsLiveData:
            MutableLiveData<Resource<List<OngoingCRMJobResponse>>> =
        MutableLiveData()

    val initiateCRMWithoutPlanLiveData:
            MutableLiveData<Resource<CRMTransactionResponse>> =
        MutableLiveData()
    val crmAddChildLiveData:
            MutableLiveData<Resource<ApiCommonResponse>> =
        MutableLiveData()

    val crmDeleteChildLiveData:
            MutableLiveData<Resource<ApiCommonResponse>> =
        MutableLiveData()
    val crmDeleteLiveData:
            MutableLiveData<Resource<ApiCommonResponse>> =
        MutableLiveData()

    fun getCRMPlanDetailById( crmPlanId: Int) {
        viewModelScope.launch {
            safeApiCRMPlanDetailsById(crmPlanId)
        }
    }
    fun initiateCRMWithoutPlan(

        request:CRMTransactionRequest
    ) {
        viewModelScope.launch {
            safeApiCallInitiateCRMWithoutPlan( request)
        }
    }
    fun getCRMPlanTranDetailById( crmTranId: Int) {
        viewModelScope.launch {
            safeApiCRMPlanTranDetailsById(crmTranId)
        }
    }
        fun fetchCrmAddChild(
            crmTransId: Int,
            tenantCode: String
        ) {

            viewModelScope.launch {
                safeCallCrmAddChild(crmTransId, tenantCode)
            }
        }
    fun processCRM(

        request: CRMTransactionRequest
    ) {
        viewModelScope.launch {
            safeApiCallInitiateSlitting( request)
        }
    }
    fun getOngoingCRMJobs( locationId: Int,process: String) {
        viewModelScope.launch {
            safeApiCallOngoingCRMJobs(locationId,process)
        }
    }

    fun fetchCRMDeleteChild(
        crmTransDetailsId: Int
    ) {

        viewModelScope.launch {

            safeCallCRMDeleteChild(
                crmTransDetailsId
            )
        }
    }
    fun fetchCRMDelete(
        crmTranId: Int
    ) {

        viewModelScope.launch {

            safeCallCRMDelete(
                crmTranId
            )
        }
    }
    private suspend fun safeCallCrmAddChild(
        crmTransId: Int,
        tenantCode: String
    ) {

        crmAddChildLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getCRMAddChild(
                        crmTransId,
                        tenantCode
                    )

                crmAddChildLiveData.postValue(
                    handleCrmAddChildResponse(response)
                )

            } else {

                crmAddChildLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            crmAddChildLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }
    private fun handleCrmAddChildResponse(
        response: Response<ApiCommonResponse>
    ): Resource<ApiCommonResponse> {

        return try {

            if (response.isSuccessful) {

                response.body()?.let {
                    return Resource.Success(it)
                }

                Resource.Error("Empty response from server")

            } else {

                val errorBody = response.errorBody()?.charStream()?.readText()

                val message = if (!errorBody.isNullOrEmpty()) {

                    val json = JSONObject(errorBody)

                    json.optString(
                        Constants.HTTP_ERROR_MESSAGE,
                        "Failed to add child"
                    )

                } else {
                    "Server error: ${response.code()}"
                }

                Resource.Error(message)
            }

        } catch (e: Exception) {

            Resource.Error(e.message ?: "Something went wrong")
        }
    }
    private suspend fun safeCallCRMDeleteChild(
        crmTransDetailsId: Int
    ) {

        crmDeleteChildLiveData.postValue(
            Resource.Loading())
        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getCRMDeleteChild(
                        crmTransDetailsId
                    )

                crmDeleteChildLiveData.postValue(
                    handleCRMDeleteChildResponse(response)
                )

            } else {

                crmDeleteChildLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            crmDeleteChildLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }
    private fun handleCRMDeleteChildResponse(
        response: Response<ApiCommonResponse>
    ): Resource<ApiCommonResponse> {

        return try {

            if (response.isSuccessful) {

                response.body()?.let {

                    return Resource.Success(it)
                }

                Resource.Error("Empty response from server")

            } else {

                val errorBody =
                    response.errorBody()
                        ?.charStream()
                        ?.readText()

                val message =
                    if (!errorBody.isNullOrEmpty()) {

                        val json = JSONObject(errorBody)

                        json.optString(
                            Constants.HTTP_ERROR_MESSAGE,
                            "Failed to delete child"
                        )

                    } else {

                        "Server error: ${response.code()}"
                    }

                Resource.Error(message)
            }

        } catch (e: Exception) {

            Resource.Error(
                e.message ?: "Something went wrong"
            )
        }
    }

    private suspend fun safeApiCallInitiateCRMWithoutPlan(

        request: CRMTransactionRequest
    ) {
        initiateCRMWithoutPlanLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.initiateCRMWithoutPlan( request)

                initiateCRMWithoutPlanLiveData.postValue(
                    handleInitiateCRMWithoutPlanResponse(response)
                )

            } else {
                initiateCRMWithoutPlanLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }
        } catch (t: Throwable) {
            initiateCRMWithoutPlanLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }
    private fun handleInitiateCRMWithoutPlanResponse(
        response: Response<CRMTransactionResponse>
    ): Resource<CRMTransactionResponse> {

        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let {
                return Resource.Success(it)
            }
        } else if (response.errorBody() != null) {

            val errorObject = JSONObject(
                response.errorBody()!!.charStream().readText()
            )

            errorMessage = errorObject.optString(
                Constants.HTTP_ERROR_MESSAGE,
                "Failed to Process Crm"
            )
        }
        return Resource.Error(errorMessage)
    }

    //  ==============================================================================
    private suspend fun safeApiCallInitiateSlitting(

        request: CRMTransactionRequest
    ) {
        ProcessCRMLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response = aplRepository.processCRM(

                    request
                )

                ProcessCRMLiveData.postValue(
                    handleProcessCRMResponse(response)
                )

            } else {
                ProcessCRMLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }
        } catch (t: Throwable) {
            ProcessCRMLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }
    private fun handleProcessCRMResponse(
        response: Response<ApiCommonResponse>
    ): Resource<ApiCommonResponse>? {

        return try {

            if (response.isSuccessful) {

                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Empty response")

            } else {

                val errorBody =
                    response.errorBody()?.string()

                Log.e("CRM_ERROR_BODY", errorBody ?: "NULL")

                val errorMessage =
                    if (!errorBody.isNullOrEmpty()) {

                        JSONObject(errorBody)
                            .optString(
                                "errorMessage",
                                "Failed to Process CRM"
                            )

                    } else {

                        "Failed to Process CRM"
                    }

                Resource.Error(errorMessage)
            }

        } catch (e: Exception) {

            Log.e("CRM_EXCEPTION", e.message.toString())

            Resource.Error(
                e.message ?: "Unknown Error"
            )
        }
    }
    private suspend fun safeApiCRMPlanDetailsById(

        crmPlanId: Int
    ) {
        CrmPlanDetailLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response: Response<CRMPlanResponse> =
                    aplRepository.getCrmPlanById( crmPlanId)

                CrmPlanDetailLiveData.postValue(
                    handleCRMPlanDetailsResponse(response)
                )

            } else {
                CrmPlanDetailLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }
        } catch (t: Throwable) {
            CrmPlanDetailLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }

    private fun handleCRMPlanDetailsResponse(
        response: Response<CRMPlanResponse>
    ): Resource<CRMPlanResponse> {

        return try {

            if (response.isSuccessful) {

                response.body()?.let {

                    return Resource.Success(it)
                }

                Resource.Error("Empty response from server")

            } else {

                val errorBody =
                    response.errorBody()
                        ?.charStream()
                        ?.readText()

                val message =
                    if (!errorBody.isNullOrEmpty()) {

                        val json = JSONObject(errorBody)

                        json.optString(
                            Constants.HTTP_ERROR_MESSAGE,
                            "Failed to delete child"
                        )

                    } else {

                        "Server error: ${response.code()}"
                    }

                Resource.Error(message)
            }

        } catch (e: Exception) {

            Resource.Error(
                e.message ?: "Something went wrong"
            )
        }
    }

    private suspend fun safeApiCRMPlanTranDetailsById(

        crmTranId: Int
    ) {
        CrmPlanTranDetailLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response: Response<CRMTransactionResponse> =
                    aplRepository.getCrmPlanTranById(crmTranId)

                CrmPlanTranDetailLiveData.postValue(
                    handleCRMPlanTranDetailsResponse(response)
                )

            } else {
                CrmPlanTranDetailLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }
        } catch (t: Throwable) {
            CrmPlanTranDetailLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }

    private fun handleCRMPlanTranDetailsResponse(
        response: Response<CRMTransactionResponse>
    ): Resource<CRMTransactionResponse> {

        var errorMessage = ""

        return try {

            if (response.isSuccessful) {

                response.body()?.let {

                    return Resource.Success(it)
                }

                Resource.Error("Empty response from server")

            } else {

                val errorBody =
                    response.errorBody()
                        ?.charStream()
                        ?.readText()

                val message =
                    if (!errorBody.isNullOrEmpty()) {

                        val json = JSONObject(errorBody)

                        json.optString(
                            Constants.HTTP_ERROR_MESSAGE,
                            "Failed to delete child"
                        )

                    } else {

                        "Server error: ${response.code()}"
                    }

                Resource.Error(message)
            }

        } catch (e: Exception) {

            Resource.Error(
                e.message ?: "Something went wrong"
            )
        }

        return Resource.Error(errorMessage)
    }
    //  =======================
    fun getCRMScan(

        barcode: String,
        crmPlanId: Int
    ) {
        viewModelScope.launch {
            safeApiCallCRMScan( barcode,crmPlanId)
        }
    }
    private suspend fun safeApiCallCRMScan(

        barcode: String,
        crmPlanId: Int
    ) {
        crmScanLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getCRMScan( barcode,crmPlanId)

                crmScanLiveData.postValue(
                    handleCRMScanResponse(response)
                )

            } else {
                crmScanLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }
        } catch (t: Throwable) {
            crmScanLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }
    private fun handleCRMScanResponse(
        response: Response<HrSlittingscanReponse>
    ): Resource<HrSlittingscanReponse> {

        var errorMessage = ""

        if (response.isSuccessful) {
            response.body()?.let {
                return Resource.Success(it)
            }
        } else if (response.errorBody() != null) {

            val errorObject = JSONObject(
                response.errorBody()!!.charStream().readText()
            )

            errorMessage = errorObject.optString(
                Constants.HTTP_ERROR_MESSAGE,
                "Failed to scan barcode"
            )
        }

        return Resource.Error(errorMessage)
    }
    //=============================================================
    fun getCRMPlannedList(


    ) {
        viewModelScope.launch {
            safeApiCallCRMPlan()
        }
    }

    private suspend fun safeApiCallCRMPlan(


    ) {
        crmPlanMutableLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getCRMPlannedList( )

                crmPlanMutableLiveData .postValue(
                    handleCRMPlanResponse(response)
                )

            } else {
                crmPlanMutableLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }
        } catch (t: Throwable) {
            crmPlanMutableLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }

    private fun handleCRMPlanResponse(
        response: Response<List<CRMPlanResponse>>
    ): Resource<List<CRMPlanResponse>> {

        var errorMessage = ""

        return try {

            if (response.isSuccessful) {

                response.body()?.let {

                    return Resource.Success(it)
                }

                Resource.Error("Empty response from server")

            } else {

                val errorBody =
                    response.errorBody()
                        ?.charStream()
                        ?.readText()

                val message =
                    if (!errorBody.isNullOrEmpty()) {

                        val json = JSONObject(errorBody)

                        json.optString(
                            Constants.HTTP_ERROR_MESSAGE,
                            "Failed to delete child"
                        )

                    } else {

                        "Server error: ${response.code()}"
                    }

                Resource.Error(message)
            }

        } catch (e: Exception) {

            Resource.Error(
                e.message ?: "Something went wrong"
            )
        }

        return Resource.Error(errorMessage)
    }
   // =========================
   private suspend fun safeApiCallOngoingCRMJobs( locationId: Int,process: String) {

       ongoingJobsLiveData.postValue(Resource.Loading())

       try {

           if (Utils.hasInternetConnection(getApplication())) {

               val response =
                   aplRepository.getOngoingCRMJobs(locationId,process)

               ongoingJobsLiveData.postValue(
                   handleOngoingJobsResponse(response)
               )

           } else {

               ongoingJobsLiveData.postValue(
                   Resource.Error(Constants.NO_INTERNET)
               )
           }

       } catch (t: Throwable) {

           ongoingJobsLiveData.postValue(
               Resource.Error(
                   t.message ?: Constants.CONFIG_ERROR
               )
           )
       }
   }


    private fun handleOngoingJobsResponse(
        response: Response<List<OngoingCRMJobResponse>>
    ): Resource<List<OngoingCRMJobResponse>> {

        var errorMessage = ""

        if (response.isSuccessful) {

            response.body()?.let {
                return Resource.Success(it)
            }

        } else if (response.errorBody() != null) {

            val errorObject = JSONObject(
                response.errorBody()!!
                    .charStream()
                    .readText()
            )

            errorMessage = errorObject.optString(
                Constants.HTTP_ERROR_MESSAGE,
                "Failed to load pickling jobs"
            )
        }

        return Resource.Error(errorMessage)
    }
    //=========================================================================================================
    private suspend fun safeCallCRMDelete(
        crmTranId: Int
    ) {

        crmDeleteLiveData.postValue(
            Resource.Loading())
        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getCRMTransactionDelete(
                        crmTranId
                    )

                crmDeleteLiveData.postValue(
                    handleCRMDeleteResponse(response)
                )

            } else {

                crmDeleteLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            crmDeleteLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }
    private fun handleCRMDeleteResponse(
        response: Response<ApiCommonResponse>
    ): Resource<ApiCommonResponse> {

        return try {

            if (response.isSuccessful) {

                response.body()?.let {

                    return Resource.Success(it)
                }

                Resource.Error("Empty response from server")

            } else {

                val errorBody =
                    response.errorBody()
                        ?.charStream()
                        ?.readText()

                val message =
                    if (!errorBody.isNullOrEmpty()) {

                        val json = JSONObject(errorBody)

                        json.optString(
                            Constants.HTTP_ERROR_MESSAGE,
                            "Failed to delete child"
                        )

                    } else {

                        "Server error: ${response.code()}"
                    }

                Resource.Error(message)
            }

        } catch (e: Exception) {

            Resource.Error(
                e.message ?: "Something went wrong"
            )
        }
    }
}