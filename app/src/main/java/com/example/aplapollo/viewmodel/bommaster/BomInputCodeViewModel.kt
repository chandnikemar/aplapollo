package com.example.aplapollo.viewmodel.bommaster

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.BoMMasterResponse

import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class BomViewModel(
    application: Application,
    private val aplRepository: APLRepository
) : AndroidViewModel(application) {

    val bomMutableLiveData:
            MutableLiveData<Resource<List<BoMMasterResponse>>> =
        MutableLiveData()

    fun getBom(inputCode: String) {
        viewModelScope.launch {
            safeApiCallGetBom(inputCode)
        }
    }

    // ==============================================================================
    private suspend fun safeApiCallGetBom(inputCode: String) {

        bomMutableLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response = aplRepository.getBomInputCode(inputCode)

                bomMutableLiveData.postValue(
                    handleBomResponse(response)
                )

            } else {
                bomMutableLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {
            bomMutableLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }

    // ==============================================================================
    private fun handleBomResponse(
        response: Response<List<BoMMasterResponse>>
    ): Resource<List<BoMMasterResponse>> {

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
                "Failed to load BOM"
            )
        }

        return Resource.Error(errorMessage)
    }
}