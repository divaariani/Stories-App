package com.dicoding.aplikasiku.storyyapp.activity.vm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.dicoding.aplikasiku.storyyapp.data.Repository
import com.dicoding.aplikasiku.storyyapp.data.Result
import com.dicoding.aplikasiku.storyyapp.model.ListStories
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import androidx.lifecycle.MutableLiveData


class MapsViewModelTest {
    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repository: Repository

    @Mock
    private lateinit var observer: Observer<Result<List<ListStories>>>
    private lateinit var mapsViewModel: MapsViewModel

    @Before
    fun setUp() {
        mapsViewModel = MapsViewModel(repository)
    }

    @Test
    fun `getUsersLocation should return success result with dummy data`() {
        val dummyListStories = listOf(
            ListStories(
                id = "1",
                name = "Location 1",
                description = "Description 1",
                photoUrl = "photo1.jpg",
                createdAt = "2023-05-26",
                lat = 1.0,
                lon = 2.0
            ),
            ListStories(
                id = "2",
                name = "Location 2",
                description = "Description 2",
                photoUrl = "photo2.jpg",
                createdAt = "2023-05-26",
                lat = 3.0,
                lon = 4.0
            )
        )
        val successResult = Result.Success(dummyListStories)
        val liveDataSuccessResult = MutableLiveData<Result<List<ListStories>>>()
        liveDataSuccessResult.value = successResult
        Mockito.`when`(repository.getLocationStory()).thenReturn(liveDataSuccessResult)
        mapsViewModel.getUsersLocation().observeForever(observer)
        Mockito.verify(observer).onChanged(successResult)
    }

    @Test
    fun `getUsersLocation should return error result`() {
        val errorResult = Result.Error("Failed To Get Location")
        val liveDataErrorResult = MutableLiveData<Result<List<ListStories>>>()
        liveDataErrorResult.value = errorResult
        Mockito.`when`(repository.getLocationStory()).thenReturn(liveDataErrorResult)
        mapsViewModel.getUsersLocation().observeForever(observer)
        Mockito.verify(observer).onChanged(errorResult)
    }
}