package com.dicoding.aplikasiku.storyyapp.activity.vm

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dicoding.aplikasiku.storyyapp.data.Injection
import com.dicoding.aplikasiku.storyyapp.model.ListStories
import com.dicoding.aplikasiku.storyyapp.data.Repository


class MainViewModel(storyRepository: Repository) : ViewModel() {

    val story: LiveData<PagingData<ListStories>> =
        storyRepository.getStoryList().cachedIn(viewModelScope)


    class ViewModelFactory(private val context: Context): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)){
                return MainViewModel(Injection.provideRepository(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}