package com.example.aplapollo.viewmodel.GP

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.ApiCommonResponse
import com.example.aplapollo.model.GP.GalvanizingTransactionRequest
import com.example.aplapollo.model.GP.GalvanizingTransactionResponse
import com.example.aplapollo.model.GP.GpOngoingJobsResponse
import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class GpViewModel  (application: Application,
                    private val aplRepository: APLRepository
) : AndroidViewModel(application) {

    val ongoingGpJobsLiveData:
            MutableLiveData<Resource<List<GpOngoingJobsResponse>>> =
        MutableLiveData()
    val gpDeleteLiveData:
            MutableLiveData<Resource<ApiCommonResponse>> =
        MutableLiveData()

    fun getOngoingGpJobs(locationId: Int,process: String) {
        viewModelScope.launch {
            safeApiCallOngoinGpJobs(locationId,process)
        }
    }
    private suspend fun safeApiCallOngoinGpJobs(locationId:Int,process: String) {

        ongoingGpJobsLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getOngoingGpJobs(locationId,process )

                ongoingGpJobsLiveData.postValue(
                    handleOngoingJobsResponse(response)
                )

            } else {

                ongoingGpJobsLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            ongoingGpJobsLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }
    private fun handleOngoingJobsResponse(
        response: Response<List<GpOngoingJobsResponse>>
    ): Resource<List<GpOngoingJobsResponse>> {

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
                "Failed to load Gp jobs"
            )
        }

        return Resource.Error(errorMessage)
    }
    //=====================================================================================================
     val initiateGpLiveData:
            MutableLiveData<Resource<String>> =
        MutableLiveData()
    fun initiateGp(request: GalvanizingTransactionRequest) {

        viewModelScope.launch {
            safeApiCallInitiateGpJobs(request)
        }
    }
    private suspend fun safeApiCallInitiateGpJobs(
        request: GalvanizingTransactionRequest
    ) {

        initiateGpLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.initiateGp(
                        request
                    )

                initiateGpLiveData.postValue(
                    handleErrorInititeResponse(response)
                )

            } else {

                initiateGpLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            initiateGpLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }

    /* Handle Response */
    private fun handleErrorInititeResponse(
        response: Response<ApiCommonResponse>
    ): Resource<String> {

        return try {

            // ================= SUCCESS =================
            if (response.isSuccessful && response.body() != null) {

                val msg =
                    response.body()?.responseMessage
                        ?: "GP completed successfully"

                return Resource.Success(msg)
            }

            // ================= ERROR =================
            val errorBody = response.errorBody()?.string()

            val errorMessage = if (!errorBody.isNullOrEmpty()) {

                val json = JSONObject(errorBody)

                // 🔥 Try multiple possible backend keys
                json.optString("errorMessage",
                    json.optString("message",
                        json.optString("responseMessage",
                            "GP process failed"
                        )
                    )
                )

            } else {
                "GP process failed"
            }

            Resource.Error(errorMessage)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
//=====================================================================================================
val gpDetailByIdLiveData:
        MutableLiveData<Resource<GalvanizingTransactionResponse>> =
    MutableLiveData()
    fun fetchGpTransactionById(galvanizingTranId: Int) {

    viewModelScope.launch {
        safeCallGpTransactionById(galvanizingTranId)
    }
}
    private suspend fun safeCallGpTransactionById(
        galvanizingTranId: Int
) {

    gpDetailByIdLiveData.postValue(Resource.Loading())

    try {

        if (Utils.hasInternetConnection(getApplication())) {

            val response =
                aplRepository.getGpDetailByID(galvanizingTranId)

            gpDetailByIdLiveData.postValue(
                handleGpTransactionResponse(response)
            )

        } else {

            gpDetailByIdLiveData.postValue(
                Resource.Error(Constants.NO_INTERNET)
            )
        }

    } catch (t: Throwable) {

        gpDetailByIdLiveData.postValue(
            Resource.Error(
                t.message ?: Constants.CONFIG_ERROR
            )
        )
    }
}


    private fun handleGpTransactionResponse(
        response: Response<GalvanizingTransactionResponse>
    ): Resource<GalvanizingTransactionResponse>{
        if (response.isSuccessful) {
            response.body()?.let { return Resource.Success(it) }
        } else if (response.errorBody() != null) {
            val errorObject = JSONObject(response.errorBody()!!.charStream().readText())
            val msg = errorObject.optString(Constants.HTTP_ERROR_MESSAGE, "Failed to load GP transaction")
            return Resource.Error(msg)
        }
        return Resource.Error("Failed to load GP transaction")
    }
    //-----------------------------------------------------------------------------------------------
    val gpAddChildLiveData:
            MutableLiveData<Resource<ApiCommonResponse>> =
        MutableLiveData()

    val gpDeleteChildLiveData:
            MutableLiveData<Resource<ApiCommonResponse>> =
        MutableLiveData()
    fun fetchGpAddChild(
        galvanizingTranId: Int,
        tenantCode: String
    ) {

        viewModelScope.launch {
            safeCallGpAddChild(galvanizingTranId, tenantCode)
        }
    }
  private suspend fun safeCallGpAddChild(
        galvanizingTranId: Int,
        tenantCode: String
    ) {

        gpAddChildLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getGpAddChild(
                        galvanizingTranId,
                        tenantCode
                    )

                gpAddChildLiveData.postValue(
                    handleGpAddChildResponse(response)
                )

            } else {

                gpAddChildLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            gpAddChildLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }
    private fun handleGpAddChildResponse(
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
    //=================================================================================================
    fun fetchGpDeleteChild(
        galvanizingTransactionDetailsId: Int,

    ) {

        viewModelScope.launch {
            safeCallGpChildDelete(galvanizingTransactionDetailsId)
        }

    }

    private suspend fun safeCallGpChildDelete(
        galvanizingTransactionDetailsId: Int

    ) {

        gpDeleteChildLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getGpDeleteChild(

                        galvanizingTransactionDetailsId

                    )

                gpDeleteChildLiveData.postValue(
                    handleGpDeleteChildResponse(response)
                )

            } else {

                gpDeleteChildLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            gpDeleteChildLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }
    private fun handleGpDeleteChildResponse(
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
    //======================================================================================================

    fun fetchGpDelete(
        galvanizingTranId: Int,

        ) {

        viewModelScope.launch {
            safeCallGpDelete(galvanizingTranId)
        }

    }

    private suspend fun safeCallGpDelete(
        galvanizingTranId: Int

    ) {

        gpDeleteLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getGPTransactionDelete(

                        galvanizingTranId

                    )

                gpDeleteLiveData.postValue(
                    handleGpDeleteResponse(response)
                )

            } else {

                gpDeleteLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            gpDeleteLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }
    private fun handleGpDeleteResponse(
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
    //=====================================================================


}