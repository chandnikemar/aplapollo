package com.example.aplapollo.viewmodel.machine

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.ProcessMachineMappingResponse
import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class MachineViewModel(
    application: Application,
    private val aplRepository: APLRepository
) : AndroidViewModel(application) {

    val machineListMutableLiveData:
            MutableLiveData<Resource<List<ProcessMachineMappingResponse>>> =
        MutableLiveData()

    fun getProcessMachine(actionTypeId: Int) {
        viewModelScope.launch {
            safeApiCallGetMachine(actionTypeId)
        }
    }

    // ============================================================
    private suspend fun safeApiCallGetMachine(actionTypeId: Int) {

        machineListMutableLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response =
                    aplRepository.getProccesMachineById(actionTypeId)

                machineListMutableLiveData.postValue(
                    handleMachineResponse(response)
                )

            } else {
                machineListMutableLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }

        } catch (t: Throwable) {
            machineListMutableLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }

    // ============================================================
    private fun handleMachineResponse(
        response: Response<List<ProcessMachineMappingResponse>>
    ): Resource<List<ProcessMachineMappingResponse>> {

        var errorMessage = ""

        if (response.isSuccessful) {

            response.body()?.let {
                return Resource.Success(it)
            }

            // ✅ Handle empty response safely
            return Resource.Success(emptyList())

        } else if (response.errorBody() != null) {

            val errorText = response.errorBody()!!.charStream().readText()

            if (errorText.isNotEmpty()) {
                val errorObject = JSONObject(errorText)

                errorMessage = errorObject.optString(
                    Constants.HTTP_ERROR_MESSAGE,
                    "Failed to load machines"
                )
            } else {
                errorMessage = "Empty error response"
            }
        }

        return Resource.Error(errorMessage)
    }
}