package com.example.submisionintermediate

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.submisionintermediate.database.Injection
import com.example.submisionintermediate.login.LoginViewModel
import com.example.submisionintermediate.main.MainViewModel
import com.example.submisionintermediate.maps.MapStoryViewModel
import com.example.submisionintermediate.model.UserPreference
import com.example.submisionintermediate.paging.StoryPagingSource
import com.example.submisionintermediate.register.RegisterViewModel

class ViewModelFactory(private val pref: UserPreference, private val context: Context) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(pref, Injection.provideRepository(context),
                    Injection.provideDatabase(context), Injection.provideApiService()) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(pref) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(pref) as T
            }
            modelClass.isAssignableFrom(MapStoryViewModel::class.java) -> {
                MapStoryViewModel(pref) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}