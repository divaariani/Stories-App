package com.dicoding.aplikasiku.storyyapp.activity.vm

import androidx.annotation.VisibleForTesting
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.dicoding.aplikasiku.storyyapp.adapter.StoryAdapter
import com.dicoding.aplikasiku.storyyapp.data.Repository
import com.dicoding.aplikasiku.storyyapp.model.ListStories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest{
    @Mock
    private lateinit var storiesRepository: Repository

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `when Get Quote Should Not Null and Return Data`() = runTest {
        val dummyStories = Dummy.generateResponseDummyStory()
        val data: PagingData<ListStories> = StoryPagingSource.snapshot(dummyStories)
        val expectedStories = MutableLiveData<PagingData<ListStories>>()
        expectedStories.value = data
        Mockito.`when`(storiesRepository.getStoryList()).thenReturn(expectedStories)

        val mainViewModel = MainViewModel(storiesRepository)
        val actualStories: PagingData<ListStories> = mainViewModel.story.getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStories)
        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStories.size, differ.snapshot().size)
        Assert.assertEquals(dummyStories[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Quote Empty Should Return No Data`() = runTest {
        val data: PagingData<ListStories> = PagingData.from(emptyList())
        val expectedStories = MutableLiveData<PagingData<ListStories>>()
        expectedStories.value = data
        Mockito.`when`(storiesRepository.getStoryList()).thenReturn(expectedStories)

        val mainViewModel = MainViewModel(storiesRepository)
        val actualStories: PagingData<ListStories> = mainViewModel.story.getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStories)
        Assert.assertEquals(0, differ.snapshot().size)
    }


    class StoryPagingSource : PagingSource<Int, LiveData<List<ListStories>>>() {
        companion object {
            fun snapshot(items: List<ListStories>): PagingData<ListStories> {
                return PagingData.from(items)
            }
        }
        override fun getRefreshKey(state: PagingState<Int, LiveData<List<ListStories>>>): Int {
            return 0
        }
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<ListStories>>> {
            return LoadResult.Page(emptyList(), 0, 1)
        }
    }

    val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
}

object Dummy {
    fun generateResponseDummyStory(): List<ListStories>{
        val items = List(10) { index ->
            ListStories(
                id = index.toString(),
                name = "Name $index",
                description = "Description $index",
                photoUrl = "http://example.com/photo$index.jpg",
                createdAt = "2023-05-26",
                lat = 0.0,
                lon = 0.0
            )
        }
        return items
    }
}

@ExperimentalCoroutinesApi
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.NONE)
fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS,
    afterObserve: () -> Unit = {}
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {

        override fun onChanged(value: T) {
            data = value
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    this.observeForever(observer)

    try {
        afterObserve.invoke()
        if (!latch.await(time, timeUnit)) {
            throw TimeoutException("LiveData value was never set.")
        }
    } finally {
        this.removeObserver(observer)
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}