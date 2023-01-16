package com.example.submisionintermediate.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.*
import com.example.submisionintermediate.ViewModelFactory
import com.example.submisionintermediate.database.Injection
import com.example.submisionintermediate.database.StoryDatabase
import com.example.submisionintermediate.model.UserModel
import com.example.submisionintermediate.model.UserPreference
import com.example.submisionintermediate.paging.StoryRemoteMediator
import com.example.submisionintermediate.paging.StoryRepository
import com.example.submisionintermediate.service.ApiConfig
import com.example.submisionintermediate.service.ApiService
import com.example.submisionintermediate.service.ListStoryItem
import com.example.submisionintermediate.service.StoriesResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val pref: UserPreference,
                    storyRepository: StoryRepository,
                    private val storyDB: StoryDatabase,
                    private val apiService: ApiService
) : ViewModel() {
    val listStories: LiveData<PagingData<ListStoryItem>> = storyRepository.getStory().cachedIn(viewModelScope)

    @OptIn(ExperimentalPagingApi::class)
    fun getStoryPaging(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDB, apiService),
            pagingSourceFactory = {
                storyDB.storyDao().getAllStory()
            }
        ).liveData
    }

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            pref.logout()
        }
    }



}
