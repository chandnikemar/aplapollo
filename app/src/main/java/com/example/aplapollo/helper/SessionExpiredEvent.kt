package com.example.aplapollo.helper

import androidx.lifecycle.MutableLiveData

object SessionExpiredEvent {
    val logoutLiveData = MutableLiveData<Boolean>()

    fun post() {
        logoutLiveData.postValue(true)
    }
}
