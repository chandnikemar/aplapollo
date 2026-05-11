package com.example.aplapollo.viewmodel.bommaster

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.BomResponse
import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class BomViewModel(
    application: Application,
    private val repository: APLRepository
) : AndroidViewModel(application) {

    val bomLiveData = MutableLiveData<Resource<List<BomResponse>>>()

    fun getBom(inputCode: String) {
        viewModelScope.launch {
            safeApiCall(inputCode)
        }
    }

    private suspend fun safeApiCall(inputCode: String) {
        bomLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response = repository.getBomInputCode(inputCode)
                bomLiveData.postValue(handleBomResponse(response))

            } else {
                bomLiveData.postValue(Resource.Error("No Internet"))
            }

        } catch (e: Exception) {
            bomLiveData.postValue(Resource.Error(e.message ?: "Error"))
        }
    }

    private fun handleBomResponse(
        response: Response<List<BomResponse>>
    ): Resource<List<BomResponse>> {

        return try {

            if (response.isSuccessful) {

                val body = response.body()

                if (body.isNullOrEmpty()) {
                    Resource.Error("No BOM data found")
                } else {
                    Resource.Success(body)
                }

            } else {

                val errorText = response.errorBody()?.string()

                val message = if (!errorText.isNullOrEmpty()) {
                    try {
                        org.json.JSONObject(errorText).optString(
                            Constants.HTTP_ERROR_MESSAGE,
                            "Something went wrong"
                        )
                    } catch (e: Exception) {
                        "Server error"
                    }
                } else {
                    "Empty error response"
                }

                Resource.Error(message)
            }

        } catch (e: Exception) {

            android.util.Log.e("BOM_PARSE_ERROR", e.message.toString())

            Resource.Error("Parsing error")
        }
    }
}