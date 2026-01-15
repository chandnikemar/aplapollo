package com.example.aplapollo.viewmodel.printlabel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.repository.APLRepository

class QcprintlabelViewModelFactory (
    private val application: Application,
    private val aplRepository: APLRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return QcPrintlabelViewModel(application, aplRepository) as T
    }
}