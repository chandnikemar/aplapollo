package com.example.aplapollo.viewmodel.slittingwithoutplan

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.repository.APLRepository

class SlittingWithoutplanViewModelfactory (

private val application: Application,
private val retrofitInstance: RetrofitInstance
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SlittingWithoutplanvViewModel::class.java)) {

            val repository = APLRepository(retrofitInstance)

            return SlittingWithoutplanvViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
