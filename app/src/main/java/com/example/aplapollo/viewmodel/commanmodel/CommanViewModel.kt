//package com.example.aplapollo.viewmodel.commanmodel
//
//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.viewModelScope
//import com.example.aplapollo.helper.Resource
//import com.example.aplapollo.model.ApiCommonResponse
//import com.example.aplapollo.repository.APLRepository
//import kotlinx.coroutines.launch
//
//class CommanViewModel (
//    application: Application,
//    private val aplRepository: APLRepository
//) : AndroidViewModel(application) {
//    val addChildLiveData:
//            MutableLiveData<Resource<ApiCommonResponse>> =
//        MutableLiveData()
//    fun fetchAddChild(
//        picklingTransId: Int,
//        tenantCode: String
//    ) {
//
//        viewModelScope.launch {
//            safeCallAddChild(picklingTransId, tenantCode)
//        }
//    }
//
//
//
//}