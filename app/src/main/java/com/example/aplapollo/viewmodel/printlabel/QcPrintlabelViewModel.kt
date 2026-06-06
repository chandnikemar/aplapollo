package com.example.aplapollo.viewmodel.printlabel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.ApiCommonResponse
import com.example.aplapollo.model.GSMResponse
import com.example.aplapollo.model.GradeResponse
import com.example.aplapollo.model.PrintLabelBarcodeRequest
import com.example.aplapollo.model.QualityCheck.PrintLabelRequest
import com.example.aplapollo.model.QualityCheck.PrintZplResponse

import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class PrintlabelViewModel(
    application: Application,
    private val aplRepository: APLRepository
) : AndroidViewModel(application) {

    val qcPrintMutableLiveData: MutableLiveData<Resource<PrintZplResponse>> =
        MutableLiveData()

    val barcodePrintLabelMutableLiveData: MutableLiveData<Resource<ApiCommonResponse>> =
        MutableLiveData()
    fun printQcLabel(


        request: PrintLabelRequest
    ) {
        viewModelScope.launch {
            safeApiCallQcPrintLabel(  request)
        }
    }
    fun printLabelBarcode(request: List<PrintLabelBarcodeRequest>) {
        viewModelScope.launch {
            safeApiCallBarcodePrintLabel(request)
        }
    }

    private suspend fun safeApiCallQcPrintLabel(
        request: PrintLabelRequest
    ) {
        qcPrintMutableLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response =
                    aplRepository.printLabelQC(request)

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

//------------------------------------------Barcode Printlabel----------------
private suspend fun safeApiCallBarcodePrintLabel(
    request: List<PrintLabelBarcodeRequest>
) {

    barcodePrintLabelMutableLiveData.postValue(Resource.Loading())

    try {

        if (!Utils.hasInternetConnection(getApplication())) {
            barcodePrintLabelMutableLiveData.postValue(
                Resource.Error(Constants.NO_INTERNET)
            )
            return
        }

        Log.d("PRINT_REQUEST", request.toString())

        val response = aplRepository.printLabelBarcode(request)

        Log.d("PRINT_HTTP_CODE", response.code().toString())
        Log.d("PRINT_HTTP_MESSAGE", response.message())

        val result = handleBarcodePrintLabelResponse(response)

        if (result != null) {
            Log.d(
                "PRINT_RESULT",
                result.javaClass.simpleName
            )
        }

        barcodePrintLabelMutableLiveData.postValue(result)

    } catch (e: Exception) {

        Log.e(
            "PRINT_EXCEPTION",
            e.stackTraceToString()
        )

        barcodePrintLabelMutableLiveData.postValue(
            Resource.Error(
                e.localizedMessage ?: "Network error"
            )
        )
    }
}

    private fun handleBarcodePrintLabelResponse(
        response: Response<ApiCommonResponse>
    ): Resource<ApiCommonResponse>? {

        val body = response.body()

        return if (response.isSuccessful && body != null) {

            if (body.statusCode == 200) {
                Resource.Success(body)
            } else {
                body.errorMessage?.let { Resource.Error(it) }
            }

        } else {

            try {
                val errorBody = response.errorBody()?.string()
                val json = JSONObject(errorBody ?: "")
                Resource.Error(json.optString("errorMessage"))
            } catch (e: Exception) {
                Resource.Error(response.message())
            }
        }
    }
    //============================================================================
    val gradeLiveData:
            MutableLiveData<Resource<List<GradeResponse>>> =
        MutableLiveData()

    fun getGrades() {

        viewModelScope.launch {
            safeApiCallGrades()
        }
    }

    private suspend fun safeApiCallGrades() {

        gradeLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getGrades()

                gradeLiveData.postValue(
                    handleGradeResponse(response)
                )

            } else {

                gradeLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            gradeLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }

    private fun handleGradeResponse(
        response: Response<List<GradeResponse>>
    ): Resource<List<GradeResponse>> {

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
                "Failed to load grades"
            )
        }

        return Resource.Error(errorMessage)
    }

    // =========================================================
    // GSM
    // =========================================================

    val gsmLiveData:
            MutableLiveData<Resource<List<GSMResponse>>> =
        MutableLiveData()

    fun getGSM() {

        viewModelScope.launch {
            safeApiCallGSM()
        }
    }

    private suspend fun safeApiCallGSM() {

        gsmLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getGSM()

                gsmLiveData.postValue(
                    handleGSMResponse(response)
                )

            } else {

                gsmLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            gsmLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }

    private fun handleGSMResponse(
        response: Response<List<GSMResponse>>
    ): Resource<List<GSMResponse>> {

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
                "Failed to load GSM"
            )
        }

        return Resource.Error(errorMessage)
    }



}
