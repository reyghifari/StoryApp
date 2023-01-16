package com.example.submisionintermediate.paging

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.submisionintermediate.login.LoginActivity
import com.example.submisionintermediate.main.MainActivity
import com.example.submisionintermediate.service.ApiService
import com.example.submisionintermediate.service.ListStoryItem


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
class StoryPagingSource(private val apiService: ApiService) : PagingSource<Int, ListStoryItem>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {



        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val responseData = apiService.getStoryPaging(
                "Bearer ${LoginActivity.token}",
                position,
                params.loadSize
            )

            MainActivity.listStories.addAll(responseData.listStory as List<ListStoryItem>)
            LoadResult.Page(
                data = MainActivity.listStories,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (MainActivity.listStories.isNullOrEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }

}