package com.example.aplapollo.viewmodel.printlabel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.repository.APLRepository

class QcprintlabelViewModelFactory (

private val application: Application,
private val retrofitInstance: RetrofitInstance
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QcPrintlabelViewModel::class.java)) {

            val repository = APLRepository(retrofitInstance)

            return QcPrintlabelViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
