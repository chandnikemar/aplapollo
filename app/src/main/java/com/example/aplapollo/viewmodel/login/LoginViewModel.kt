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

class LoginViewModel (
application: Application,
private val aplRepository: APLRepository
) : AndroidViewModel(application) {
    val loginMutableLiveData: MutableLiveData<Resource<LoginResponse>> = MutableLiveData()

    fun login(
        baseUrl: String,
        loginRequest: LoginRequest
    ) {
        viewModelScope.launch {
            safeAPICallDtmsLogin(baseUrl, loginRequest)
        }
    }

    private fun handleDtmsUserLoginResponse(response: Response<LoginResponse>): Resource<LoginResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { Response ->
                return Resource.Success(Response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }

    private suspend fun safeAPICallDtmsLogin(baseUrl: String, loginRequest: LoginRequest) {
        loginMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = aplRepository.login( baseUrl,loginRequest)
                loginMutableLiveData.postValue(handleDtmsUserLoginResponse(response))
            } else {
                loginMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    loginMutableLiveData.postValue(Resource.Error("${t.message}"))
                }
                else -> loginMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
}