package com.example.aplapollo.viewmodel.slitting

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.Slitting.HrSlittingPlanResponse

import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class SlittingViewModel(
    application: Application,
    private val aplRepository: APLRepository
) : AndroidViewModel(application) {

    val hrSlittingPlanMutableLiveData:
            MutableLiveData<Resource<List<HrSlittingPlanResponse>>> =
        MutableLiveData()

    val hrSlittingPlanDetailLiveData: MutableLiveData<Resource<HrSlittingPlanResponse>> =
        MutableLiveData()
    fun getHrSlittingPlannedList(
        baseUrl: String,

    ) {
        viewModelScope.launch {
            safeApiCallHrSlittingPlan(baseUrl)
        }
    }
    fun getHrSlittingPlanById(baseUrl: String, hrSlittingPlanId: Int) {
        viewModelScope.launch {
            safeApiCallHrSlittingPlanById(baseUrl, hrSlittingPlanId)
        }
    }
    private suspend fun safeApiCallHrSlittingPlan(
        baseUrl: String,

    ) {
        hrSlittingPlanMutableLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getHrPlannedList( baseUrl)

                hrSlittingPlanMutableLiveData.postValue(
                    handleHrSlittingPlanResponse(response)
                )

            } else {
                hrSlittingPlanMutableLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }
        } catch (t: Throwable) {
            hrSlittingPlanMutableLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }

    private fun handleHrSlittingPlanResponse(
        response: Response<List<HrSlittingPlanResponse>>
    ): Resource<List<HrSlittingPlanResponse>> {

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
                "Failed to load slitting plan"
            )
        }

        return Resource.Error(errorMessage)
    }
  //  ==============================================================================
  private suspend fun safeApiCallHrSlittingPlanById(baseUrl: String, hrSlittingPlanId: Int) {
      hrSlittingPlanDetailLiveData.postValue(Resource.Loading())

      try {
          if (Utils.hasInternetConnection(getApplication())) {
              val response = aplRepository.getHrSlittingPlanById(baseUrl, hrSlittingPlanId)
              hrSlittingPlanDetailLiveData.postValue(handleHrSlittingPlanByIdResponse(response))
          } else {
              hrSlittingPlanDetailLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
          }
      } catch (t: Throwable) {
          hrSlittingPlanDetailLiveData.postValue(Resource.Error(t.message ?: Constants.CONFIG_ERROR))
      }
  }
    private fun handleHrSlittingPlanByIdResponse(
        response: Response<HrSlittingPlanResponse>
    ): Resource<HrSlittingPlanResponse> {

        var errorMessage = ""

        if (response.isSuccessful) {
            response.body()?.let {
                return Resource.Success(it)
            }
        } else if (response.errorBody() != null) {
            val errorObject = JSONObject(response.errorBody()!!.charStream().readText())
            errorMessage = errorObject.optString(Constants.HTTP_ERROR_MESSAGE, "Failed to load plan details")
        }

        return Resource.Error(errorMessage)
    }
}
