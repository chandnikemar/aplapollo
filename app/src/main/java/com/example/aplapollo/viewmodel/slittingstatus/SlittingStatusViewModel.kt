package com.example.aplapollo.viewmodel.slittingstatus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.ApiCommonResponse
import com.example.aplapollo.model.Slitting.HrSlittingDetailsResponse
import com.example.aplapollo.model.Slitting.HrSlittingTransactionRequest
import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class SlittingStatusViewModel(
    application: Application,
    private val aplRepository: APLRepository
) : AndroidViewModel(application) {

    val hrSlittingDetailsLiveData: MutableLiveData<Resource<HrSlittingDetailsResponse>> =
        MutableLiveData()

    val completeHrSlittingLiveData: MutableLiveData<Resource<ApiCommonResponse>> =
        MutableLiveData()

    fun getHrSlittingDetailsById(
        baseUrl: String,
        tranId: Int
    ) {
        viewModelScope.launch {
            safeApiCallHrSlittingDetailsById(baseUrl, tranId)
        }
    }
    fun completeHrSlitting(
        baseUrl: String,
        request: HrSlittingTransactionRequest
    ) {
        viewModelScope.launch {
            safeApiCallCompleteHrSlitting(baseUrl, request)
        }
    }

    private suspend fun safeApiCallHrSlittingDetailsById(
        baseUrl: String,
        tranId: Int
    ) {
        hrSlittingDetailsLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response: Response<HrSlittingDetailsResponse> =
                    aplRepository.getHrSlittingDetailsById(baseUrl, tranId)

                hrSlittingDetailsLiveData.postValue(
                    handleHrSlittingDetailsResponse(response)
                )

            } else {
                hrSlittingDetailsLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }
        } catch (t: Throwable) {
            hrSlittingDetailsLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }

    private fun handleHrSlittingDetailsResponse(
        response: Response<HrSlittingDetailsResponse>
    ): Resource<HrSlittingDetailsResponse> {

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
                "Failed to load slitting details"
            )
        }

        return Resource.Error(errorMessage)
    }
  //  ==================================================Slitting complte APi
  private suspend fun safeApiCallCompleteHrSlitting(
      baseUrl: String,
      request: HrSlittingTransactionRequest
  ) {
      completeHrSlittingLiveData.postValue(Resource.Loading())

      try {
          if (Utils.hasInternetConnection(getApplication())) {

              val response =
                  aplRepository.completeHRSlitting(baseUrl, request)

              completeHrSlittingLiveData.postValue(
                  handleCompleteHrSlittingResponse(response)
              )

          } else {
              completeHrSlittingLiveData.postValue(
                  Resource.Error(Constants.NO_INTERNET)
              )
          }
      } catch (t: Throwable) {
          completeHrSlittingLiveData.postValue(
              Resource.Error(t.message ?: Constants.CONFIG_ERROR)
          )
      }
  }
    private fun handleCompleteHrSlittingResponse(
        response: Response<ApiCommonResponse>
    ): Resource<ApiCommonResponse> {

        var errorMessage = "Failed to complete HR Slitting"

        if (response.isSuccessful) {
            response.body()?.let {
                return Resource.Success(it)
            }
        } else if (response.errorBody() != null) {

            val errorObject = JSONObject(
                response.errorBody()!!.charStream().readText()
            )

            // Backend sends: errorMessage, statusCode = 422
            errorMessage = errorObject.optString(
                "errorMessage",
                errorMessage
            )
        }

        return Resource.Error(errorMessage)
    }

}

