package com.dicoding.aplikasiku.storyyapp.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.aplikasiku.storyyapp.model.ListStories
import com.dicoding.aplikasiku.storyyapp.api.ApiService
import com.dicoding.aplikasiku.storyyapp.data.Result.Success
import com.dicoding.aplikasiku.storyyapp.model.AllStoryResponse
import com.dicoding.aplikasiku.storyyapp.utils.SessionManager
import retrofit2.Response

class Repository(private val apiService: ApiService, private val context: Context) {

    private val getStoryLocation = MediatorLiveData<com.dicoding.aplikasiku.storyyapp.data.Result<List<ListStories>>>()

    fun getLocationStory(): LiveData<com.dicoding.aplikasiku.storyyapp.data.Result<List<ListStories>>>{
        val client = apiService.storyLocation("Bearer ${SessionManager(context).getToken().token}", 1)
        client.enqueue(object : retrofit2.Callback<AllStoryResponse> {
            override fun onResponse(
                call: retrofit2.Call<AllStoryResponse>,
                response: Response<AllStoryResponse>
            ) {
                if (response.isSuccessful){
                    val storiesLocation = response.body()?.listStory
                    if (storiesLocation != null){
                        getStoryLocation.value = Success(storiesLocation)
                    }
                } else{
                    getStoryLocation.value = com.dicoding.aplikasiku.storyyapp.data.Result.Error("Failed To Get Location")
                }
            }

            override fun onFailure(call: retrofit2.Call<AllStoryResponse>, t: Throwable) {
                getStoryLocation.value = com.dicoding.aplikasiku.storyyapp.data.Result.Error("Failed To Get Location")
            }

        })
        return getStoryLocation
    }

    fun getStoryList(): LiveData<PagingData<ListStories>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            pagingSourceFactory = {
                PagingSource(apiService, context)
            }
        ).liveData
    }
}