package com.example.aplapollo.viewmodel.Pickling

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.repository.APLRepository

class PicklingViewModelfactory (
    private val application: Application,
    private val retrofitInstance: RetrofitInstance
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PicklingViewModel::class.java)) {

                val repository = APLRepository(retrofitInstance)

                return PicklingViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }