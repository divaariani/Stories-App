package com.dicoding.aplikasiku.storyyapp.data

import android.content.Context
import com.dicoding.aplikasiku.storyyapp.api.ApiConfig

object Injection {
    fun provideRepository( context: Context): Repository {
        val apiService = ApiConfig.getApiService()
        return Repository(apiService, context)
    }
}