package com.example.submisionintermediate.database

import android.content.Context
import com.example.submisionintermediate.paging.StoryRepository
import com.example.submisionintermediate.service.ApiConfig
import com.example.submisionintermediate.service.ApiService

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig().getApiService()
        return StoryRepository(apiService, database)
    }

    fun provideDatabase(context: Context): StoryDatabase {
        return StoryDatabase.getDatabase(context)
    }

    fun provideApiService(): ApiService{
        return  ApiConfig().getApiService()
    }
}