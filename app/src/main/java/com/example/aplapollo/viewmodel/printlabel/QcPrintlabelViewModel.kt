package com.example.aplapollo.viewmodel.printlabel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.QualityCheck.PrintLabelRequest
import com.example.aplapollo.model.QualityCheck.PrintZplResponse

import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class QcPrintlabelViewModel(
    application: Application,
    private val aplRepository: APLRepository
) : AndroidViewModel(application) {

    val qcPrintMutableLiveData: MutableLiveData<Resource<PrintZplResponse>> =
        MutableLiveData()

    fun printQcLabel(
        baseUrl: String,

        request: PrintLabelRequest
    ) {
        viewModelScope.launch {
            safeApiCallQcPrintLabel(baseUrl,  request)
        }
    }

    private suspend fun safeApiCallQcPrintLabel(
        baseUrl: String,

        request: PrintLabelRequest
    ) {
        qcPrintMutableLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response =
                    aplRepository.printLabelQC( baseUrl, request)

                qcPrintMutableLiveData.postValue(
                    handleQcPrintLabelResponse(response)
                )
            } else {
                qcPrintMutableLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    qcPrintMutableLiveData.postValue(
                        Resource.Error(t.message ?: "Unknown error")
                    )
                }
                else -> qcPrintMutableLiveData.postValue(
                    Resource.Error(Constants.CONFIG_ERROR)
                )
            }
        }
    }

    private fun handleQcPrintLabelResponse(
        response: Response<PrintZplResponse>
    ): Resource<PrintZplResponse> {

        var errorMessage = ""

        if (response.isSuccessful) {
            response.body()?.let {
                return Resource.Success(it)
            }
        } else if (response.errorBody() != null) {
            val errorObject = JSONObject(
                response.errorBody()!!.charStream().readText()
            )
            errorMessage =
                errorObject.optString(Constants.HTTP_ERROR_MESSAGE, "Print failed")
        }

        return Resource.Error(errorMessage)
    }
}
