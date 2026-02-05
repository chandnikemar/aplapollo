package com.example.aplapollo.viewmodel.slittingwithoutplan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.Slitting.ApiResponse
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
                Resource.Error(body?.responseMessage ?: "No stock found")
            }
        } else {
            Resource.Error("Failed to fetch stock data")
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

        var errorMessage = "Failed to initiate slitting"

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
                errorMessage
            )
        }

        return Resource.Error(errorMessage)
    }


}
