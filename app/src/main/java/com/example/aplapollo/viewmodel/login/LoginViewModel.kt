package com.example.aplapollo.viewmodel.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.login.LoginRequest
import com.example.aplapollo.model.login.LoginResponse
import com.example.aplapollo.repository.APLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class LoginViewModel(
    application: Application,
    private val aplRepository: APLRepository
) : AndroidViewModel(application) {

    val loginMutableLiveData: MutableLiveData<Resource<LoginResponse>> =
        MutableLiveData()

    // ✅ NO baseUrl here anymore
    fun login(loginRequest: LoginRequest) {
        viewModelScope.launch {
            safeLoginCall(loginRequest)
        }
    }

    private suspend fun safeLoginCall(loginRequest: LoginRequest) {
        loginMutableLiveData.postValue(Resource.Loading())

        try {
            if (Utils.hasInternetConnection(getApplication())) {

                val response = aplRepository.login(loginRequest)
                loginMutableLiveData.postValue(
                    handleLoginResponse(response)
                )

            } else {
                loginMutableLiveData.postValue(
                    Resource.Error(Constants.NO_INTERNET)
                )
            }
        } catch (t: Throwable) {
            loginMutableLiveData.postValue(
                Resource.Error(t.message ?: Constants.CONFIG_ERROR)
            )
        }
    }

    private fun handleLoginResponse(
        response: Response<LoginResponse>
    ): Resource<LoginResponse> {

        if (response.isSuccessful) {
            response.body()?.let {
                return Resource.Success(it)
            }
        }

        var errorMessage = Constants.CONFIG_ERROR
        response.errorBody()?.let {
            val errorObject =
                JSONObject(it.charStream().readText())
            errorMessage =
                errorObject.optString(
                    Constants.HTTP_ERROR_MESSAGE,
                    errorMessage
                )
        }

        return Resource.Error(errorMessage)
    }
}
