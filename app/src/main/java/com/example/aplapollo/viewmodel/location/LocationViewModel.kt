package com.example.aplapollo.viewmodel.location

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.LocationPaginationRequest
import com.example.aplapollo.model.LocationResponse
import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class LocationViewModel(
    application: Application,
    private val aplRepository: APLRepository
) : AndroidViewModel(application) {

    val locationListMutableLiveData:
            MutableLiveData<Resource<List<LocationResponse>>> =
        MutableLiveData()

    fun getLocations(

      request: LocationPaginationRequest
    ) {
        viewModelScope.launch {
            safeApiCallGetLocations( request)
        }
    }

    // ==============================================================================
    private suspend fun safeApiCallGetLocations(

       request: LocationPaginationRequest
    ) {
        locationListMutableLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {



                val response =
                    aplRepository.getLocations( request)

                locationListMutableLiveData.postValue(
                    handleLocationResponse(response)
                )

            } else {
                locationListMutableLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }
        } catch (t: Throwable) {
            locationListMutableLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }

    private fun handleLocationResponse(
        response: Response<List<LocationResponse>>
    ): Resource<List<LocationResponse>> {

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
                "Failed to load locations"
            )
        }

        return Resource.Error(errorMessage)
    }
}
