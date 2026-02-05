package com.example.aplapollo.viewmodel.login

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.repository.APLRepository


class LoginViewModelFactory(
    private val application: Application,
    private val retrofitInstance: RetrofitInstance
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {

            val repository = APLRepository(retrofitInstance)

            return LoginViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
