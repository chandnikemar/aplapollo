package com.example.aplapollo.viewmodel.Pickling

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.ApiCommonResponse
import com.example.aplapollo.model.Pickling.PicklingJobInProgressResponse
import com.example.aplapollo.model.Pickling.PicklingTransactionResponse
import com.example.aplapollo.model.Pickling.ProcessPicklingRequest
import com.example.aplapollo.model.Slitting.ApiResponse
import com.example.aplapollo.model.Slitting.StockBarcodeWithoutplanResponse
import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class PicklingViewModel(
    application: Application,
    private val aplRepository: APLRepository
) : AndroidViewModel(application) {

    val ongoingJobsLiveData:
            MutableLiveData<Resource<List<PicklingJobInProgressResponse>>> =
        MutableLiveData()


    val picklingBarcodeLiveData:
            MutableLiveData<Resource<ApiResponse<StockBarcodeWithoutplanResponse>>> =
        MutableLiveData()

    val processPicklingLiveData:
            MutableLiveData<Resource<String>> =
        MutableLiveData()


    val picklingTransactionLiveData:
            MutableLiveData<Resource<PicklingTransactionResponse>> =
        MutableLiveData()

    val picklingAddChildLiveData:
            MutableLiveData<Resource<ApiCommonResponse>> =
        MutableLiveData()

    val picklingDeleteChildLiveData:
            MutableLiveData<Resource<ApiCommonResponse>> =
        MutableLiveData()


    fun getOngoingPicklingJobs(locationId: Int) {
        viewModelScope.launch {
            safeApiCallOngoingPicklingJobs(locationId)
        }
    }
    fun fetchPicklingBarcodeData(code: String?) {

        viewModelScope.launch {
            safeCallPicklingBarcode(code)
        }
    }
    fun submitPickling(request: ProcessPicklingRequest) {

        viewModelScope.launch {
            safeSubmitPickling(request)
        }
    }
    fun fetchPicklingTransactionById(picklingTranId: Int) {

        viewModelScope.launch {
            safeCallPicklingTransactionById(picklingTranId)
        }
    }


    fun fetchPicklingAddChild(
        picklingTransId: Int,
        tenantCode: String
    ) {

        viewModelScope.launch {
            safeCallPicklingAddChild(picklingTransId, tenantCode)
        }
    }
    fun fetchPicklingDeleteChild(
        picklingTransDetailsId: Int
    ) {

        viewModelScope.launch {

            safeCallPicklingDeleteChild(
                picklingTransDetailsId
            )
        }
    }
    private suspend fun safeCallPicklingDeleteChild(
        picklingTransDetailsId: Int
    ) {

        picklingDeleteChildLiveData.postValue(
            Resource.Loading()
        )

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getPicklingDeleteChild(
                        picklingTransDetailsId
                    )

                picklingDeleteChildLiveData.postValue(
                    handlePicklingDeleteChildResponse(response)
                )

            } else {

                picklingDeleteChildLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            picklingDeleteChildLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }
    private fun handlePicklingDeleteChildResponse(
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
    private suspend fun safeApiCallOngoingPicklingJobs(locationId:Int) {

        ongoingJobsLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getOngoingPicklingJobs(locationId)

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
        response: Response<List<PicklingJobInProgressResponse>>
    ): Resource<List<PicklingJobInProgressResponse>> {

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
    //=======================================================================
    private suspend fun safeCallPicklingBarcode(code: String?) {

        picklingBarcodeLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getStockBarcodePicklingdata(code)
                picklingBarcodeLiveData.postValue(
                    handlePicklingBarcodeResponse(response)
                )

            } else {

                picklingBarcodeLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            picklingBarcodeLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }


    /* Handle Response */
    private fun handlePicklingBarcodeResponse(
        response: Response<ApiResponse<StockBarcodeWithoutplanResponse>>
    ): Resource<ApiResponse<StockBarcodeWithoutplanResponse>> {

        return try {

            if (response.isSuccessful) {

                response.body()?.let {
                    return Resource.Success(it)
                }

                Resource.Error("Empty response from server")

            } else {

                val errorBody = response.errorBody()?.string()

                val errorMessage = if (!errorBody.isNullOrEmpty()) {

                    val jsonObject = JSONObject(errorBody)

                    // Try different possible keys
                    jsonObject.optString("message",
                        jsonObject.optString("error",
                            jsonObject.optString("statusMessage",
                                "Barcode not found"
                            )
                        )
                    )

                } else {
                    "Server error : ${response.code()}"
                }

                Resource.Error(errorMessage)
            }

        } catch (e: Exception) {

            Resource.Error("Something went wrong : ${e.localizedMessage}")
        }
    }

    private suspend fun safeSubmitPickling(
        request: ProcessPicklingRequest
    ) {

        processPicklingLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.processPickling(
                        request
                    )

                processPicklingLiveData.postValue(
                    handleProcessPicklingResponse(response)
                )

            } else {

                processPicklingLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            processPicklingLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }


    /* Handle Response */
    private fun handleProcessPicklingResponse(
        response: Response<ApiCommonResponse>
    ): Resource<String> {

        return try {

            // ================= SUCCESS =================
            if (response.isSuccessful && response.body() != null) {

                val msg =
                    response.body()?.responseMessage
                        ?: "Pickling completed successfully"

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
                            "Pickling process failed"
                        )
                    )
                )

            } else {
                "Pickling process failed"
            }

            Resource.Error(errorMessage)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
    private suspend fun safeCallPicklingTransactionById(
        picklingTranId: Int
    ) {

        picklingTransactionLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getPicklingTransactionById(picklingTranId)

                picklingTransactionLiveData.postValue(
                    handlePicklingTransactionResponse(response)
                )

            } else {

                picklingTransactionLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            picklingTransactionLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }


    private fun handlePicklingTransactionResponse(
        response: Response<PicklingTransactionResponse>
    ): Resource<PicklingTransactionResponse>{
        if (response.isSuccessful) {
            response.body()?.let { return Resource.Success(it) }
        } else if (response.errorBody() != null) {
            val errorObject = JSONObject(response.errorBody()!!.charStream().readText())
            val msg = errorObject.optString(Constants.HTTP_ERROR_MESSAGE, "Failed to load pickling transaction")
            return Resource.Error(msg)
        }
        return Resource.Error("Failed to load pickling transaction")
    }

    private suspend fun safeCallPicklingAddChild(
        picklingTransId: Int,
        tenantCode: String
    ) {

        picklingAddChildLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getPicklingAddChild(
                        picklingTransId,
                        tenantCode
                    )

                picklingAddChildLiveData.postValue(
                    handlePicklingAddChildResponse(response)
                )

            } else {

                picklingAddChildLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            picklingAddChildLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }
    private fun handlePicklingAddChildResponse(
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
}





