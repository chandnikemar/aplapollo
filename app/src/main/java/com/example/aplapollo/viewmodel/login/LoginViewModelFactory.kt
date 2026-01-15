package com.example.aplapollo.viewmodel.login

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.repository.APLRepository


class LoginViewModelFactory(
    private val application: Application,
    private val aplRepository: APLRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(application, aplRepository) as T
    }
}