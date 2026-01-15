package com.example.aplapollo.viewmodel.location

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.repository.APLRepository

class LocationViewModelFactory (
    private val application: Application,
    private val aplRepository: APLRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LocationViewModel(application, aplRepository) as T
    }
}