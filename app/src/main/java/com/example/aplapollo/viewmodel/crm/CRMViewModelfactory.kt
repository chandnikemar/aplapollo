package com.example.aplapollo.viewmodel.crm

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.repository.APLRepository

class CRMViewModelfactory (
    private val application: Application,
    private val retrofitInstance: RetrofitInstance
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CRMViewModel::class.java)) {

            val repository = APLRepository(retrofitInstance)

            return CRMViewModel(application, repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
