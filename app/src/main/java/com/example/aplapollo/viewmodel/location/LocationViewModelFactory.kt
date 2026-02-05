package com.example.aplapollo.viewmodel.location

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.repository.APLRepository

class LocationViewModelFactory (

private val application: Application,
private val retrofitInstance: RetrofitInstance
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {

            val repository = APLRepository(retrofitInstance)

            return LocationViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
