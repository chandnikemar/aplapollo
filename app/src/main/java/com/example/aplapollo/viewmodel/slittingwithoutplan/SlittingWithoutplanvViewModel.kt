package com.example.aplapollo.viewmodel.slittingwithoutplan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.ApiCommonResponse
import com.example.aplapollo.model.Slitting.ApiResponse
import com.example.aplapollo.model.Slitting.CoilSplitRequest
import com.example.aplapollo.model.Slitting.InitiateSlittingResponse
import com.example.aplapollo.model.Slitting.InitiateSlittingWithoutPlanRequest
import com.example.aplapollo.model.Slitting.StockBarcodeWithoutplanResponse
import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class SlittingWithoutplanvViewModel(
    application: Application,
    private val aplRepository: APLRepository
)  : AndroidViewModel(application) {

    val stockByBarcodeLiveData:
            MutableLiveData<Resource<StockBarcodeWithoutplanResponse>> =
        MutableLiveData()


    val initiateSlittingWithoutPlanLiveData:
            MutableLiveData<Resource<InitiateSlittingResponse>> =
        MutableLiveData()
    fun getStockByBatchOrBarcode(

        code: String?
    ) {
        viewModelScope.launch {
            safeApiCallGetStockByBatchOrBarcode(


                code
            )
        }
    }

    fun initiateSlittingWithoutPlan(

        request: InitiateSlittingWithoutPlanRequest
    ) {
        viewModelScope.launch {
            safeApiCallInitiateSlittingWithoutPlan( request)
        }
    }

    private suspend fun safeApiCallGetStockByBatchOrBarcode(

        code: String?
    ) {
        stockByBarcodeLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getStockByBatchOrBarcode(


                        code
                    )

                stockByBarcodeLiveData.postValue(
                    handleStockByBatchOrBarcodeResponse(response)
                )

            } else {
                stockByBarcodeLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }
        } catch (t: Throwable) {
            stockByBarcodeLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }

    private fun handleStockByBatchOrBarcodeResponse(
        response: Response<ApiResponse<StockBarcodeWithoutplanResponse>>
    ): Resource<StockBarcodeWithoutplanResponse> {

        return if (response.isSuccessful) {

            val body = response.body()

            if (body?.responseObject != null) {

                Resource.Success(body.responseObject)

            } else {

                Resource.Error(
                    body?.errorMessage
                        ?: body?.responseMessage
                        ?: "No stock found"
                )
            }

        } else {

            try {

                val errorBody =
                    response.errorBody()?.string()

                val errorObject =
                    JSONObject(errorBody ?: "")

                val backendMessage =

                    errorObject.optString(
                        "errorMessage"
                    ).ifEmpty {

                        errorObject.optString(
                            "responseMessage"
                        )
                    }.ifEmpty {

                        errorObject.optString(
                            "message"
                        )
                    }

                Resource.Error(
                    backendMessage.ifEmpty {
                        "Failed to fetch stock data"
                    }
                )

            } catch (e: Exception) {

                Resource.Error(
                    e.message ?: "Unknown error"
                )
            }
        }
    }


    //==================================================================================================
private suspend fun safeApiCallInitiateSlittingWithoutPlan(

    request: InitiateSlittingWithoutPlanRequest
) {
    initiateSlittingWithoutPlanLiveData.postValue(Resource.Loading())

    try {
        if (Utils.hasInternetConnection(getApplication())) {

            val response =
                aplRepository.initiateSlittingWithoutPlan( request)

            initiateSlittingWithoutPlanLiveData.postValue(
                handleInitiateSlittingWithoutPlanResponse(response)
            )

        } else {
            initiateSlittingWithoutPlanLiveData.postValue(
                Resource.Error(Constants.NO_INTERNET)
            )
        }
    } catch (t: Throwable) {
        initiateSlittingWithoutPlanLiveData.postValue(
            Resource.Error(t.message ?: Constants.CONFIG_ERROR)
        )
    }
}
    private fun handleInitiateSlittingWithoutPlanResponse(
        response: Response<InitiateSlittingResponse>
    ): Resource<InitiateSlittingResponse> {

        return if (response.isSuccessful) {

            response.body()?.let {

                Resource.Success(it)

            } ?: Resource.Error("Empty response from server")

        } else {

            try {

                val errorBody =
                    response.errorBody()?.string()

                val errorObject =
                    JSONObject(errorBody ?: "")

                val backendMessage =

                    errorObject.optString(
                        "errorMessage"
                    ).ifEmpty {

                        errorObject.optString(
                            "responseMessage"
                        )
                    }.ifEmpty {

                        errorObject.optString(
                            "message"
                        )
                    }

                Resource.Error(
                    backendMessage.ifEmpty {
                        "Something went wrong"
                    }
                )

            } catch (e: Exception) {

                Resource.Error(
                    e.message ?: "Unknown error"
                )
            }
        }
    }
    //================
    val coilSplitLiveData: MutableLiveData<Resource<ApiCommonResponse>> =
        MutableLiveData()

    fun coilSplit(request: CoilSplitRequest) {
        viewModelScope.launch {
            safeApiCallCoilSplit(request)
        }
    }

    private suspend fun safeApiCallCoilSplit(
        request: CoilSplitRequest
    ) {

        coilSplitLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response = aplRepository.coilSplit(request)

                coilSplitLiveData.postValue(
                    handleCoilSplitResponse(response)
                )

            } else {

                coilSplitLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            coilSplitLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }

    private fun handleCoilSplitResponse(
        response: Response<ApiCommonResponse>
    ): Resource<ApiCommonResponse> {

        return if (response.isSuccessful) {

            response.body()?.let {
                Resource.Success(it)
            } ?: Resource.Error("Empty response from server")

        } else {

            try {

                val errorBody = response.errorBody()?.string()
                val errorObject = JSONObject(errorBody ?: "")

                val backendMessage =
                    errorObject.optString("errorMessage")
                        .ifEmpty { errorObject.optString("responseMessage") }
                        .ifEmpty { errorObject.optString("message") }

                Resource.Error(
                    backendMessage.ifEmpty { "Coil split failed" }
                )

            } catch (e: Exception) {

                Resource.Error(
                    e.message ?: "Unknown error"
                )
            }
        }
    }

}
