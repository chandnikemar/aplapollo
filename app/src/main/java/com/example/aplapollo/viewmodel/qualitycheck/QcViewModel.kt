package com.example.aplapollo.viewmodel.qualitycheck

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.QualityCheck.BarcodePrefixResponse
import com.example.aplapollo.model.QualityCheck.MaterialTypeResponse
import com.example.aplapollo.model.QualityCheck.QCFetchRequest
import com.example.aplapollo.model.QualityCheck.QCFetchResponse
import com.example.aplapollo.model.QualityCheck.QCStatusSubmissionRequest
import com.example.aplapollo.model.QualityCheck.QCStatusSubmissionResponse
import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class QCViewModel(
    application: Application,
    private val aplRepository: APLRepository
) : AndroidViewModel(application) {


    val qcFetchLiveData = MutableLiveData<Resource<QCFetchResponse>>()
    val barcodeLiveData = MutableLiveData<Resource<BarcodePrefixResponse>>()
    val qcStatusLiveData = MutableLiveData<Resource<QCStatusSubmissionResponse>>()
    val materialtypeLiveData = MutableLiveData<Resource<MaterialTypeResponse>>()


    /* ----------------------------------------------------------------------
     *                        FETCH QC DATA
     * ---------------------------------------------------------------------- */
    fun fetchQCData( request: QCFetchRequest) {
        viewModelScope.launch {
            safeAPICallFetchQC( request)
        }
    }


    private fun handleQCFetchResponse(response: Response<QCFetchResponse>): Resource<QCFetchResponse> {

        return when {
            response.isSuccessful -> {
                response.body()?.let { Resource.Success(it) }
                    ?: Resource.Error("Empty Response")
            }

            response.errorBody() != null -> {
                val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
                Resource.Error(errorObj.optString(Constants.HTTP_ERROR_MESSAGE, "Something went wrong"))
            }

            else -> Resource.Error("Something went wrong")
        }
    }

    private suspend fun safeAPICallFetchQC(


        request: QCFetchRequest
    ) {
        qcFetchLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response = aplRepository.fetchQCData(  request)
                qcFetchLiveData.postValue(handleQCFetchResponse(response))

            } else qcFetchLiveData.postValue(Resource.Error(Constants.NO_INTERNET))

        } catch (t: Throwable) {
            qcFetchLiveData.postValue(Resource.Error(t.message ?: Constants.CONFIG_ERROR))
        }
    }





    /* ----------------------------------------------------------------------
     *                        QC SUBMISSION
     * ---------------------------------------------------------------------- */
    fun submitQCStatus(


        request: QCStatusSubmissionRequest
    ) {
        viewModelScope.launch {
            safeAPICallSubmit( request)
        }
    }

    private fun handleSubmitResponse(
        response: Response<QCStatusSubmissionResponse>
    ): Resource<QCStatusSubmissionResponse> {

        return if (response.isSuccessful) {

            response.body()?.let {
                Resource.Success(
                    data = it,
                    message = it.responseMessage
                )
            } ?: Resource.Error("Empty response from server")

        } else {

            val errorMessage = try {
                val errorBody = response.errorBody()?.string()
                JSONObject(errorBody ?: "")
                    .optString("errorMessage", "Something went wrong")
            } catch (e: Exception) {
                "Something went wrong"
            }

            Resource.Error(errorMessage)
        }
    }


    private suspend fun safeAPICallSubmit(
   
        request: QCStatusSubmissionRequest
    ) {
        qcStatusLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response = aplRepository.submitQCStatus( request)
                qcStatusLiveData.postValue(handleSubmitResponse(response))

            } else {
                qcStatusLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }

        } catch (t: Throwable) {
            qcStatusLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }

}

