package com.example.submisionintermediate.login

import android.util.Log
import androidx.lifecycle.*
import com.example.submisionintermediate.model.UserModel
import com.example.submisionintermediate.model.UserPreference
import com.example.submisionintermediate.service.ApiConfig
import com.example.submisionintermediate.service.LoginResponse
import com.example.submisionintermediate.utils.Helper
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONTokener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(private val pref: UserPreference) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toast = MutableLiveData<String>()
    val toast: LiveData<String> = _toast

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    private val _token = MutableLiveData<String>()
    val token: LiveData<String> = _token

    fun loginUser(user: UserModel) {
        _isLoading.value = true
        val service = ApiConfig().getApiService().login(user.email!!, user.password!!)

        service.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                _isLoading.value = false

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        _toast.value = responseBody.message
                        _name.value = responseBody.loginResult.name
                        _token.value = responseBody.loginResult.token
                        login(user)
                    }
                }
                else
                    _toast.value = response.message()
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _isLoading.value = false
                _toast.value = "Gagal instance Retrofit"
            }
        })
    }

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    private fun login(user: UserModel) {
        viewModelScope.launch {
            pref.login(user)
        }
    }
}