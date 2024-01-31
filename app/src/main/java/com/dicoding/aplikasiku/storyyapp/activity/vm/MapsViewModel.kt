package com.dicoding.aplikasiku.storyyapp.activity.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.aplikasiku.storyyapp.data.Injection
import com.dicoding.aplikasiku.storyyapp.data.Repository

class MapsViewModel(private val storyRepository: Repository): ViewModel() {
    fun getUsersLocation() = storyRepository.getLocationStory()

    class ViewModelFactory(private val context: Context): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapsViewModel::class.java)){
                return MapsViewModel(Injection.provideRepository(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}