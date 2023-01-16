package com.example.submisionintermediate.paging

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.example.submisionintermediate.database.StoryDatabase
import com.example.submisionintermediate.service.ApiService
import com.example.submisionintermediate.service.ListStoryItem

class StoryRepository(private val apiService: ApiService, private val storyDatabase: StoryDatabase) {
    @OptIn(ExperimentalPagingApi::class)
    fun getStory(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }
}