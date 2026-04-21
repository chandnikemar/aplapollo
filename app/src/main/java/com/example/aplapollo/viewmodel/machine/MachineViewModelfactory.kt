package com.example.aplapollo.viewmodel.machine

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.repository.APLRepository

class MachineViewModelfactory (
    private val application: Application,
    private val retrofitInstance: RetrofitInstance
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MachineViewModel::class.java)) {

            val repository = APLRepository(retrofitInstance)

            return MachineViewModel(application, repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

