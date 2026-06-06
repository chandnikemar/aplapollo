package com.example.aplapollo.viewmodel.slitting

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.ApiCommonResponse
import com.example.aplapollo.model.Slitting.HrSlittingItemAgainstPlanRequest
import com.example.aplapollo.model.Slitting.HrSlittingItemAgainstPlanResponse
import com.example.aplapollo.model.Slitting.HrSlittingPlanResponse
import com.example.aplapollo.model.Slitting.HrSlittingscanReponse
import com.example.aplapollo.model.Slitting.InitiateSlittingRequest
import com.example.aplapollo.model.Slitting.InitiateSlittingResponse
import com.example.aplapollo.model.Slitting.OngoingJobResponse

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
            MutableLiveData<Resource<List<OngoingJobResponse>>> =
        MutableLiveData()

    fun getHrSlittingPlannedList(


    ) {
        viewModelScope.launch {
            safeApiCallHrSlittingPlan()
        }
    }
    fun getHrSlittingPlanById( hrSlittingPlanId: Int) {
        viewModelScope.launch {
            safeApiCallHrSlittingPlanById(hrSlittingPlanId)
        }
    }
    fun getHrSlittingScan(

        barcode: String,
        hrSlittingPlanId: Int
    ) {
        viewModelScope.launch {
            safeApiCallHrSlittingScan( barcode,hrSlittingPlanId)
        }
    }
    fun getAllItemAgainstPlan(

        request: HrSlittingItemAgainstPlanRequest
    ) {
        viewModelScope.launch {
            safeApiCallItemAgainstPlan( request)
        }
    }
    fun initiateHrSlitting(

        request: InitiateSlittingRequest
    ) {
        viewModelScope.launch {
            safeApiCallInitiateSlitting(request)
        }
    }

    fun getOngoingSlittingJobs(locationId: Int,process: String) {
        viewModelScope.launch {
            safeApiCallOngoingJobs(locationId,process)
        }
    }


    private suspend fun safeApiCallHrSlittingPlan(


    ) {
        hrSlittingPlanMutableLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getHrPlannedList( )

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
  private suspend fun safeApiCallHrSlittingPlanById( hrSlittingPlanId: Int) {
      hrSlittingPlanDetailLiveData.postValue(Resource.Loading())

      try {
          if (Utils.hasInternetConnection(getApplication())) {
              val response = aplRepository.getHrSlittingPlanById( hrSlittingPlanId)
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

       barcode: String,
       hrSlittingPlanId: Int
   ) {
       hrSlittingScanLiveData.postValue(Resource.Loading())

       try {
           if (Utils.hasInternetConnection(getApplication())) {

               val response =
                   aplRepository.getHrSlittingScan( barcode,hrSlittingPlanId)

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

       request: HrSlittingItemAgainstPlanRequest
    ) {
        hrItemAgainstPlanLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response = aplRepository.getAllItemAgainstPlan(

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

    request: InitiateSlittingRequest
) {
    initiateSlittingLiveData.postValue(Resource.Loading())

    try {
        if (Utils.hasInternetConnection(getApplication())) {

            val response = aplRepository.initiateHrSlitting(

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
    private suspend fun safeApiCallOngoingJobs(locationId: Int,process:String) {

        ongoingJobsLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response = aplRepository.getOngoingJobs(locationId,process)

                // 🔥 DEBUG
                Log.d("API_DEBUG", "Response Code = ${response.code()}")
                Log.d("API_DEBUG", "Response Body = ${response.body()}")

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
        response: Response<List<OngoingJobResponse>>
    ): Resource<List<OngoingJobResponse>> {

        return if (response.isSuccessful) {

            val jobs = response.body() ?: emptyList()

            Log.d("API_DEBUG", "Jobs size = ${jobs.size}")

            Resource.Success(jobs)

        } else {

            val error = response.errorBody()?.string()
            Resource.Error(error ?: "Failed to load ongoing jobs")
        }
    }
    //=======================================================================================
    val deleteSlittingTranLiveData:
            MutableLiveData<Resource<ApiCommonResponse>> =
        MutableLiveData()
    fun deleteSlittingTransaction(
        HRSlittingTranId: Int
    ) {
        viewModelScope.launch {
            safeApiCallDeleteSlittingTransaction(
                HRSlittingTranId
            )
        }
    }
    private suspend fun safeApiCallDeleteSlittingTransaction(
        HRSlittingTranId: Int
    ) {

        deleteSlittingTranLiveData.postValue(
            Resource.Loading()
        )

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getSlittingTransactionDelete(
                        HRSlittingTranId
                    )

                deleteSlittingTranLiveData.postValue(
                    handleDeleteSlittingTransactionResponse(
                        response
                    )
                )

            } else {

                deleteSlittingTranLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            deleteSlittingTranLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }
    private fun handleDeleteSlittingTransactionResponse(
        response: Response<ApiCommonResponse>
    ): Resource<ApiCommonResponse> {

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
                "Failed to delete transaction"
            )
        }

        return Resource.Error(errorMessage)
    }

}

