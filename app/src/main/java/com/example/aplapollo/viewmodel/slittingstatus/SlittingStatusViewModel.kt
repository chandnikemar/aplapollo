package com.example.aplapollo.viewmodel.slittingstatus

import HrSlittingStatusResponse
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.ApiCommonResponse
import com.example.aplapollo.model.Slitting.ApplicationConfigMaster
import com.example.aplapollo.model.Slitting.HrSlittingCompleteRequest
import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class SlittingStatusViewModel(
    application: Application,
    private val aplRepository: APLRepository
) : AndroidViewModel(application) {

    val hrSlittingDetailsLiveData:
            MutableLiveData<Resource<HrSlittingStatusResponse>> =
        MutableLiveData()

    val completeHrSlittingLiveData:
            MutableLiveData<Resource<ApiCommonResponse>> =
        MutableLiveData()

    // =========================
    // ADD CHILD
    // =========================

    val addChildLiveData:
            MutableLiveData<Resource<ApiCommonResponse>> =
        MutableLiveData()

    // =========================
    // DELETE CHILD
    // =========================

    val deleteChildLiveData:
            MutableLiveData<Resource<ApiCommonResponse>> =
        MutableLiveData()

    // =====================================================
    // GET DETAILS
    // =====================================================

    fun getHrSlittingDetailsById(
        tranId: Int
    ) {
        viewModelScope.launch {
            safeApiCallHrSlittingDetailsById(tranId)
        }
    }

    // =====================================================
    // COMPLETE
    // =====================================================

    fun completeHrSlitting(
        request: HrSlittingCompleteRequest
    ) {
        viewModelScope.launch {
            safeApiCallCompleteHrSlitting(request)
        }
    }

    // =====================================================
    // ADD CHILD
    // =====================================================

    fun getSlittingAddChild(
        hrSlittingTransId: Int,
        tenantCode: String
    ) {
        viewModelScope.launch {
            safeApiCallAddChild(
                hrSlittingTransId,
                tenantCode
            )
        }
    }

    // =====================================================
    // DELETE CHILD
    // =====================================================

    fun getSlittingDeleteChild(
        hrSlittingTransDetailsId: Int
    ) {
        viewModelScope.launch {
            safeApiCallDeleteChild(
                hrSlittingTransDetailsId
            )
        }
    }

    // =====================================================
    // DETAILS API
    // =====================================================

    private suspend fun safeApiCallHrSlittingDetailsById(
        tranId: Int
    ) {

        hrSlittingDetailsLiveData.postValue(
            Resource.Loading()
        )

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response:
                        Response<HrSlittingStatusResponse> =

                    aplRepository.getHrSlittingDetailsById(
                        tranId
                    )

                hrSlittingDetailsLiveData.postValue(
                    handleHrSlittingDetailsResponse(response)
                )

            } else {

                hrSlittingDetailsLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            hrSlittingDetailsLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }

    private fun handleHrSlittingDetailsResponse(
        response: Response<HrSlittingStatusResponse>
    ): Resource<HrSlittingStatusResponse> {

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
                "Failed to load slitting details"
            )
        }

        return Resource.Error(errorMessage)
    }

    // =====================================================
    // COMPLETE API
    // =====================================================

    private suspend fun safeApiCallCompleteHrSlitting(
        request: HrSlittingCompleteRequest
    ) {

        completeHrSlittingLiveData.postValue(
            Resource.Loading()
        )

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.completeHRSlitting(
                        request
                    )

                completeHrSlittingLiveData.postValue(
                    handleCompleteHrSlittingResponse(response)
                )

            } else {

                completeHrSlittingLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            completeHrSlittingLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }

    private fun handleCompleteHrSlittingResponse(
        response: Response<ApiCommonResponse>
    ): Resource<ApiCommonResponse> {

        var errorMessage =
            "Failed to complete HR Slitting"

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
                "errorMessage",
                errorMessage
            )
        }

        return Resource.Error(errorMessage)
    }

    // =====================================================
    // ADD CHILD API
    // =====================================================

    private suspend fun safeApiCallAddChild(
        hrSlittingTransId: Int,
        tenantCode: String
    ) {

        addChildLiveData.postValue(
            Resource.Loading()
        )

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getSlittingAddChild(
                        hrSlittingTransId,
                        tenantCode
                    )

                addChildLiveData.postValue(
                    handleCommonResponse(response)
                )

            } else {

                addChildLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            addChildLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }

    // =====================================================
    // DELETE CHILD API
    // =====================================================

    private suspend fun safeApiCallDeleteChild(
        hrSlittingTransDetailsId: Int
    ) {

        deleteChildLiveData.postValue(
            Resource.Loading()
        )

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getSlittingDeleteChild(
                        hrSlittingTransDetailsId
                    )

                deleteChildLiveData.postValue(
                    handleCommonResponse(response)
                )

            } else {

                deleteChildLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {

            deleteChildLiveData.postValue(
                Resource.Error(
                    t.message ?: Constants.CONFIG_ERROR
                )
            )
        }
    }

    // =====================================================
    // COMMON RESPONSE
    // =====================================================

    private fun handleCommonResponse(
        response: Response<ApiCommonResponse>
    ): Resource<ApiCommonResponse> {

        var errorMessage = "Something went wrong"

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
                "errorMessage",
                errorMessage
            )
        }

        return Resource.Error(errorMessage)
    }
    //============================================================================================
    val configKeyLiveData =
        MutableLiveData<Resource<ApplicationConfigMaster>>()

    val registerConfigLiveData =
        MutableLiveData<Resource<ApiCommonResponse>>()
    fun getConfigByKey(key: String) {
        viewModelScope.launch {
            safeApiCallGetConfigByKey(key)
        }
    }
    fun registerConfig(request: ApplicationConfigMaster) {
        viewModelScope.launch {
            safeApiCallRegisterConfig(request)
        }
    }
    private suspend fun safeApiCallGetConfigByKey(key: String) {

        configKeyLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getAppConfigKey(key)

                configKeyLiveData.postValue(
                    handleConfigKeyResponse(response)
                )

            } else {
                configKeyLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }

        } catch (t: Throwable) {

            configKeyLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }
    private fun handleConfigKeyResponse(
        response: Response<ApplicationConfigMaster>
    ): Resource<ApplicationConfigMaster> {

        var errorMessage = "Failed to get config"

        if (response.isSuccessful) {

            response.body()?.let {
                return Resource.Success(it)
            }

        } else if (response.errorBody() != null) {

            val errorObject = JSONObject(
                response.errorBody()!!.charStream().readText()
            )

            errorMessage = errorObject.optString(
                "errorMessage",
                errorMessage
            )
        }

        return Resource.Error(errorMessage)
    }
    private suspend fun safeApiCallRegisterConfig(
        request: ApplicationConfigMaster
    ) {

        registerConfigLiveData.postValue(Resource.Loading())

        try {

            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getRegisterConfig(request)

                registerConfigLiveData.postValue(
                    handleRegisterConfigResponse(response)
                )

            } else {
                registerConfigLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }

        } catch (t: Throwable) {

            registerConfigLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }
    private fun handleRegisterConfigResponse(
        response: Response<ApiCommonResponse>
    ): Resource<ApiCommonResponse> {

        var errorMessage = "Failed to register config"

        if (response.isSuccessful) {

            response.body()?.let {
                return Resource.Success(it)
            }

        } else if (response.errorBody() != null) {

            val errorObject = JSONObject(
                response.errorBody()!!.charStream().readText()
            )

            errorMessage = errorObject.optString(
                "errorMessage",
                errorMessage
            )
        }

        return Resource.Error(errorMessage)
    }
}