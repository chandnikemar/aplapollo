package com.example.aplapollo.viewmodel.qualitycheck


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.repository.APLRepository


class QcViewModelFactory (
    private val application: Application,
    private val retrofitInstance: RetrofitInstance
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QCViewModel::class.java)) {

            val repository = APLRepository(retrofitInstance)

            return QCViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
