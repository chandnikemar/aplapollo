package com.example.aplapollo.viewmodel.slittingstatus

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.repository.APLRepository

class SlittingStatusViewModelfactory (

private val application: Application,
private val retrofitInstance: RetrofitInstance
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SlittingStatusViewModel::class.java)) {

            val repository = APLRepository(retrofitInstance)

            return SlittingStatusViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
