package com.example.aplapollo.viewmodel.actiontype

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.ActionTypeResponse
import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class ActionTypeViewModel(
    application: Application,
    private val aplRepository: APLRepository
) : AndroidViewModel(application) {

    val actionTypeListMutableLiveData =
        MutableLiveData<Resource<List<ActionTypeResponse>>>()

        fun getActionTypes() {
        viewModelScope.launch {
            safeApiCallGetActionType()
        }
    }

    private suspend fun safeApiCallGetActionType() {
        actionTypeListMutableLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response = aplRepository.getActionTypeList()


                Log.d("ACTION_API", "Response Code: ${response.code()}")

                actionTypeListMutableLiveData.postValue(
                    handleActionTypeResponse(response)
                )

            } else {
                actionTypeListMutableLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }
        } catch (t: Throwable) {

            Log.e("ACTION_API_ERROR", t.message.toString())

            actionTypeListMutableLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }

    private fun handleActionTypeResponse(
        response: Response<List<ActionTypeResponse>>
    ): Resource<List<ActionTypeResponse>> {

        return try {

            if (response.isSuccessful) {

                val body = response.body()


                if (body.isNullOrEmpty()) {
                    Resource.Error("No data found")
                } else {
                    Resource.Success(body)
                }

            } else {


                val errorText = response.errorBody()?.string()

                val message = if (!errorText.isNullOrEmpty()) {
                    try {
                        JSONObject(errorText).optString(
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

            Log.e("PARSE_ERROR", e.message.toString())

            Resource.Error("Parsing error")
        }
    }
}