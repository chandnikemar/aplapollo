package com.example.aplapollo.viewmodel.slitting

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.Slitting.HrSlittingItemAgainstPlanRequest
import com.example.aplapollo.model.Slitting.HrSlittingItemAgainstPlanResponse
import com.example.aplapollo.model.Slitting.HrSlittingPlanResponse
import com.example.aplapollo.model.Slitting.HrSlittingscanReponse
import com.example.aplapollo.model.Slitting.InitiateSlittingRequest
import com.example.aplapollo.model.Slitting.InitiateSlittingResponse
import com.example.aplapollo.model.Slitting.OngoingSlittingJobResponse

import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class SlittingViewModel(
    application: Application,
    private val aplRepository: APLRepository
) : AndroidViewModel(application) {

        val hrSlittingPlanMutableLiveData: MutableLiveData<Resource<List<HrSlittingPlanResponse>>> =
            MutableLiveData()

        val hrSlittingPlanDetailLiveData: MutableLiveData<Resource<HrSlittingPlanResponse>> =
        MutableLiveData()

        val hrSlittingScanLiveData: MutableLiveData<Resource<HrSlittingscanReponse>> =
        MutableLiveData()

    val hrItemAgainstPlanLiveData :
            MutableLiveData<Resource<List<HrSlittingItemAgainstPlanResponse>>> =
        MutableLiveData()

    val initiateSlittingLiveData: MutableLiveData<Resource<InitiateSlittingResponse>> =
        MutableLiveData()

    val ongoingJobsLiveData:
            MutableLiveData<Resource<List<OngoingSlittingJobResponse>>> =
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
    fun getHrSlittingScan(
        baseUrl: String,
        barcode: String,
        hrSlittingPlanId: Int
    ) {
        viewModelScope.launch {
            safeApiCallHrSlittingScan(baseUrl, barcode,hrSlittingPlanId)
        }
    }
    fun getAllItemAgainstPlan(
        baseUrl: String,
        request: HrSlittingItemAgainstPlanRequest
    ) {
        viewModelScope.launch {
            safeApiCallItemAgainstPlan(baseUrl, request)
        }
    }
    fun initiateHrSlitting(
        baseUrl: String,
        request: InitiateSlittingRequest
    ) {
        viewModelScope.launch {
            safeApiCallInitiateSlitting(baseUrl, request)
        }
    }

    fun getOngoingSlittingJobs(baseUrl: String) {
        viewModelScope.launch {
            safeApiCallOngoingJobs(baseUrl)
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
   // =====================================================================================================
   private suspend fun safeApiCallHrSlittingScan(
       baseUrl: String,
       barcode: String,
       hrSlittingPlanId: Int
   ) {
       hrSlittingScanLiveData.postValue(Resource.Loading())

       try {
           if (Utils.hasInternetConnection(getApplication())) {

               val response =
                   aplRepository.getHrSlittingScan(baseUrl, barcode,hrSlittingPlanId)

               hrSlittingScanLiveData.postValue(
                   handleHrSlittingScanResponse(response)
               )

           } else {
               hrSlittingScanLiveData.postValue(
                   Resource.Error(Constants.NO_INTERNET)
               )
           }
       } catch (t: Throwable) {
           hrSlittingScanLiveData.postValue(
               Resource.Error(t.message ?: Constants.CONFIG_ERROR)
           )
       }
   }
    private fun handleHrSlittingScanResponse(
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
    //==============================================================================================

    private suspend fun safeApiCallItemAgainstPlan(
        baseUrl: String,
       request: HrSlittingItemAgainstPlanRequest
    ) {
        hrItemAgainstPlanLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response = aplRepository.getAllItemAgainstPlan(
                    baseUrl,
                 request
                )

                hrItemAgainstPlanLiveData.postValue(
                    handleItemAgainstPlanResponse(response)
                )

            } else {
                hrItemAgainstPlanLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }
        } catch (t: Throwable) {
            hrItemAgainstPlanLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }

    private fun handleItemAgainstPlanResponse(
        response: Response<List<HrSlittingItemAgainstPlanResponse>>
    ): Resource<List<HrSlittingItemAgainstPlanResponse>> {

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
                "Failed to load item against plan"
            )
        }

        return Resource.Error(errorMessage)
    }
//    ==================================================================================
private suspend fun safeApiCallInitiateSlitting(
    baseUrl: String,
    request: InitiateSlittingRequest
) {
    initiateSlittingLiveData.postValue(Resource.Loading())

    try {
        if (Utils.hasInternetConnection(getApplication())) {

            val response = aplRepository.initiateHrSlitting(
                baseUrl,
                request
            )

            initiateSlittingLiveData.postValue(
                handleInitiateSlittingResponse(response)
            )

        } else {
            initiateSlittingLiveData.postValue(
                Resource.Error(Constants.NO_INTERNET)
            )
        }
    } catch (t: Throwable) {
        initiateSlittingLiveData.postValue(
            Resource.Error(t.message ?: Constants.CONFIG_ERROR)
        )
    }
}
    private fun handleInitiateSlittingResponse(
        response: Response<InitiateSlittingResponse>
    ): Resource<InitiateSlittingResponse> {

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
                "Failed to initiate slitting"
            )
        }

        return Resource.Error(errorMessage)
    }
    //=========================================================================================
    private suspend fun safeApiCallOngoingJobs(baseUrl: String) {
        ongoingJobsLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response = aplRepository.getOngoingJobs(baseUrl)

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
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }
    private fun handleOngoingJobsResponse(
        response: Response<List<OngoingSlittingJobResponse>>
    ): Resource<List<OngoingSlittingJobResponse>> {

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
                "Failed to load ongoing jobs"
            )
        }

        return Resource.Error(errorMessage)
    }

}

