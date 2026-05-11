package com.example.aplapollo.viewmodel.gateentry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.ApiCommonResponse
import com.example.aplapollo.model.GateEntry.CoilSubmitRequest
import com.example.aplapollo.model.GateEntry.GateEntryResponse
import com.example.aplapollo.model.GateEntry.GateTransactionRequest
import com.example.aplapollo.model.GateEntry.GateTransactionResponse
import com.example.aplapollo.model.GateEntry.TransporterResponse
import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class GateTransactionViewModel(
    application: Application,
    private val aplRepository: APLRepository
) : AndroidViewModel(application) {

    val gateTransactionLiveData: MutableLiveData<Resource<ApiCommonResponse>> =
        MutableLiveData()

    val gateEntryLiveData: MutableLiveData<Resource<GateTransactionResponse>> =
        MutableLiveData()

    val gateEntryListLiveData: MutableLiveData<Resource<List<GateEntryResponse>>> =
        MutableLiveData()

    val gateEntryUpdateLiveData: MutableLiveData<Resource<List<GateEntryResponse>>> =
        MutableLiveData()

    val transporterListLiveData :MutableLiveData<Resource<List<TransporterResponse>>> =
        MutableLiveData()

    fun createGateEntry(request: GateTransactionRequest) {
        viewModelScope.launch {
            safeApiCallGateEntry(request)
        }
    }
    fun saveCoilItem(
        request: CoilSubmitRequest
    ) {
        viewModelScope.launch {
            safeApiCallSaveGateTransactionItem(request)
        }
    }
    fun getGateEntryList() {
        viewModelScope.launch {
            safeApiCallGateEntryList()
        }
    }
    fun getGateEntryUpdate(gateTransactionId: Int) {
        viewModelScope.launch {
            safeApiCallGateEntryUpdate(gateTransactionId)
        }
    }
    fun getTransporterList(){
        viewModelScope.launch { safeApiCallTransporterList() }
    }


    private suspend fun safeApiCallSaveGateTransactionItem(
        request: CoilSubmitRequest
    ) {

        gateTransactionLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.saveGateTransactionItem(request)

                gateTransactionLiveData.postValue(
                    handleSaveGateTransactionResponse(response)
                )

            } else {

                gateTransactionLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            gateTransactionLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }

    private fun handleSaveGateTransactionResponse(
        response: Response<ApiCommonResponse>
    ): Resource<ApiCommonResponse> {

        return try {

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {

                val errorBody = response.errorBody()?.string()

                val errorMessage = try {
                    if (!errorBody.isNullOrEmpty()) {

                        val json = JSONObject(errorBody)

                        json.optString(
                            "errorMessage",   // ✅ IMPORTANT FIX
                            json.optString(
                                "responseMessage",
                                "Something went wrong"
                            )
                        )
                    } else {
                        "Something went wrong"
                    }
                } catch (e: Exception) {
                    "Something went wrong"
                }

                Resource.Error(errorMessage)
            }

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
   // ===========================================================================================
   private suspend fun safeApiCallGateEntry(
       request: GateTransactionRequest
   ) {

       gateEntryLiveData.postValue(Resource.Loading())

       try {
           if (Utils.hasInternetConnection(getApplication())) {

               val response = aplRepository.gateTransactionEntry(request)

               gateEntryLiveData.postValue(
                   handleGateEntryResponse(response)
               )

           } else {
               gateEntryLiveData.postValue(
                   Resource.Error(Constants.NO_INTERNET)
               )
           }

       } catch (t: Throwable) {
           gateEntryLiveData.postValue(
               Resource.Error(t.message ?: Constants.CONFIG_ERROR)
           )
       }
   }

    private fun handleGateEntryResponse(
        response: Response<GateTransactionResponse>
    ): Resource<GateTransactionResponse> {

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
                "Failed to create gate entry"
            )
        }

        return Resource.Error(errorMessage)
    }
    //------------------------------------------------------------------------------------------
    private suspend fun safeApiCallGateEntryList() {

        gateEntryListLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response = aplRepository.getGateEntryList()

                gateEntryListLiveData.postValue(
                    handleGateEntryListResponse(response)
                )

            } else {
                gateEntryListLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            gateEntryListLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }
    private fun handleGateEntryListResponse(
        response: Response<List<GateEntryResponse>>
    ): Resource<List<GateEntryResponse>> {

        return try {

            if (response.isSuccessful && response.body() != null) {

                Resource.Success(response.body()!!)

            } else {

                val errorBody = response.errorBody()?.string()

                val errorMessage = try {
                    if (!errorBody.isNullOrEmpty()) {

                        val json = JSONObject(errorBody)

                        json.optString(
                            "errorMessage",
                            json.optString("responseMessage", "Something went wrong")
                        )

                    } else {
                        "Something went wrong"
                    }
                } catch (e: Exception) {
                    "Something went wrong"
                }

                Resource.Error(errorMessage)
            }

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
    //------------------------------------------------------------------------------------------
    private suspend fun safeApiCallGateEntryUpdate(gateTransactionId: Int) {

        gateEntryUpdateLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getGateEntryUpdate(gateTransactionId)

                gateEntryUpdateLiveData.postValue(
                    handleGateEntryUpdateResponse(response)
                )

            } else {
                gateEntryUpdateLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {
            gateEntryUpdateLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }

    // 🔹 Handle Response
    private fun handleGateEntryUpdateResponse(
        response: Response<List<GateEntryResponse>>
    ): Resource<List<GateEntryResponse>> {

        return try {

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {

                val errorBody = response.errorBody()?.string()

                val errorMessage = try {
                    if (!errorBody.isNullOrEmpty()) {
                        val json = JSONObject(errorBody)

                        json.optString(
                            "errorMessage",
                            json.optString("responseMessage", "Something went wrong")
                        )
                    } else {
                        "Something went wrong"
                    }
                } catch (e: Exception) {
                    "Something went wrong"
                }

                Resource.Error(errorMessage)
            }

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
    private suspend fun safeApiCallTransporterList() {

        transporterListLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response = aplRepository.getTransporterList()

                transporterListLiveData.postValue(
                    handleTransporterResponse(response)
                )

            } else {
                transporterListLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            transporterListLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }
    private fun handleTransporterResponse(
        response: Response<List<TransporterResponse>>
    ): Resource<List<TransporterResponse>> {

        return try {

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {

                val errorBody = response.errorBody()?.string()

                val errorMessage = try {
                    if (!errorBody.isNullOrEmpty()) {

                        val json = JSONObject(errorBody)

                        json.optString(
                            "errorMessage",
                            json.optString("responseMessage", "Something went wrong")
                        )
                    } else {
                        "Something went wrong"
                    }
                } catch (e: Exception) {
                    "Something went wrong"
                }

                Resource.Error(errorMessage)
            }

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}