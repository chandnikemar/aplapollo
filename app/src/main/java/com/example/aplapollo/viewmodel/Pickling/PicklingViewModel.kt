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



    fun getOngoingPicklingJobs() {
        viewModelScope.launch {
            safeApiCallOngoingPicklingJobs()
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




    private suspend fun safeApiCallOngoingPicklingJobs() {

        ongoingJobsLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getOngoingPicklingJobs()

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
                "Failed to fetch barcode data"
            )
        }

        return Resource.Error(errorMessage)
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

        var errorMessage = ""

        if (response.isSuccessful) {

            response.body()?.let {

                // If API returns message field
                val msg = it.responseMessage ?: "Pickling completed successfully"

                return Resource.Success(msg)
            }

        } else if (response.errorBody() != null) {

            val errorObject = JSONObject(
                response.errorBody()!!
                    .charStream()
                    .readText()
            )

            errorMessage = errorObject.optString(
                Constants.HTTP_ERROR_MESSAGE,
                "Pickling process failed"
            )
        }

        return Resource.Error(errorMessage)
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
    ): Resource<PicklingTransactionResponse> {
        if (response.isSuccessful) {
            response.body()?.let { return Resource.Success(it) }
        } else if (response.errorBody() != null) {
            val errorObject = JSONObject(response.errorBody()!!.charStream().readText())
            val msg = errorObject.optString(Constants.HTTP_ERROR_MESSAGE, "Failed to load pickling transaction")
            return Resource.Error(msg)
        }
        return Resource.Error("Failed to load pickling transaction")
    }
}





