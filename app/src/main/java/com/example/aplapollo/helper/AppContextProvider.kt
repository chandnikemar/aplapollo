package com.example.aplapollo.helper

import android.app.Application
import android.content.Context

class AppContextProvider : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        lateinit var context: Context
            private set
    }
}