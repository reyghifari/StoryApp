package com.example.submisionintermediate.maps

import androidx.lifecycle.*
import com.example.submisionintermediate.model.UserModel
import com.example.submisionintermediate.model.UserPreference
import com.example.submisionintermediate.service.ApiConfig
import com.example.submisionintermediate.service.ListStoryItem
import com.example.submisionintermediate.service.StoriesResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapStoryViewModel(private val pref: UserPreference): ViewModel() {
    private val _listStory = MutableLiveData<List<ListStoryItem>>()
    val listStories: LiveData<List<ListStoryItem>> = _listStory


    fun getAllStoryMap(token: String) {
        val service = ApiConfig().getApiService().getAllStoriesMap(token, (1).toString())

        service.enqueue(object : Callback<StoriesResponse> {
            override fun onResponse(call: Call<StoriesResponse>, response: Response<StoriesResponse>) {

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        _listStory.value = (responseBody.listStory as List<ListStoryItem>?)!!
                    }
                }
            }
            override fun onFailure(call: Call<StoriesResponse>, t: Throwable) {
                //DO NOTHING
            }
        })
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