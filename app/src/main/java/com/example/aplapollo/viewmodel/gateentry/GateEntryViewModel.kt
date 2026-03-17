package com.example.aplapollo.viewmodel.gateentry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.ApiCommonResponse
import com.example.aplapollo.model.GateEntry.CoilSubmitRequest
import com.example.aplapollo.model.GateEntry.GateTransactionRequest
import com.example.aplapollo.model.GateEntry.GateTransactionResponse
import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class GateTransactionViewModel(
    application: Application,
    private val aplRepository: APLRepository
) : AndroidViewModel(application) {

    val gateTransactionLiveData: MutableLiveData<Resource<ApiCommonResponse>> =
        MutableLiveData()

    val gateEntryLiveData: MutableLiveData<Resource<GateTransactionResponse>> =
        MutableLiveData()
    fun createGateEntry(request: GateTransactionRequest) {
        viewModelScope.launch {
            safeApiCallGateEntry(request)
        }
    }
    fun saveCoilItem(
        request: CoilSubmitRequest
    ) {
        viewModelScope.launch {
            safeApiCallSaveGateTransactionItem(request)
        }
    }


    private suspend fun safeApiCallSaveGateTransactionItem(
        request: CoilSubmitRequest
    ) {

        gateTransactionLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.saveGateTransactionItem(request)

                gateTransactionLiveData.postValue(
                    handleSaveGateTransactionResponse(response)
                )

            } else {

                gateTransactionLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            gateTransactionLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }

    private fun handleSaveGateTransactionResponse(
        response: Response<ApiCommonResponse>
    ): Resource<ApiCommonResponse> {

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
                "Failed to save gate transaction item"
            )
        }

        return Resource.Error(errorMessage)
    }
   // ===========================================================================================
   private suspend fun safeApiCallGateEntry(
       request: GateTransactionRequest
   ) {

       gateEntryLiveData.postValue(Resource.Loading())

       try {
           if (Utils.hasInternetConnection(getApplication())) {

               val response = aplRepository.gateTransactionEntry(request)

               gateEntryLiveData.postValue(
                   handleGateEntryResponse(response)
               )

           } else {
               gateEntryLiveData.postValue(
                   Resource.Error(Constants.NO_INTERNET)
               )
           }

       } catch (t: Throwable) {
           gateEntryLiveData.postValue(
               Resource.Error(t.message ?: Constants.CONFIG_ERROR)
           )
       }
   }

    private fun handleGateEntryResponse(
        response: Response<GateTransactionResponse>
    ): Resource<GateTransactionResponse> {

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
                "Failed to create gate entry"
            )
        }

        return Resource.Error(errorMessage)
    }

}