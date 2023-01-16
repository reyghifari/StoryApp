package com.example.submisionintermediate.register


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.submisionintermediate.model.UserModel
import com.example.submisionintermediate.model.UserPreference
import com.example.submisionintermediate.service.ApiConfig
import com.example.submisionintermediate.service.ApiResponse
import com.example.submisionintermediate.utils.Helper
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONTokener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel(private val pref: UserPreference) : ViewModel() {

    fun register(name: String, email: String, pass: String, callback: Helper.ApiCallbackString){
        val service = ApiConfig().getApiService().register(name, email, pass )
        service.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(
                call: Call<ApiResponse>,
                response: Response<ApiResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error)
                        setUser(UserModel(name,email,pass))
                        callback.onResponse(response.body() != null, SUCCESS)
                } else {
                    Log.e(TAG, "onFailure1: ${response.message()}")
                    val jsonObject = JSONTokener(response.errorBody()!!.string()).nextValue() as JSONObject
                    val message = jsonObject.getString("message")
                    callback.onResponse(false, message)
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e(TAG, "onFailure2: ${t.message}")
                callback.onResponse(false, t.message.toString())
            }
        })
    }

    fun setUser(user: UserModel){
        viewModelScope.launch {
            pref.saveUser(user)
        }
    }
    companion object {
        private const val TAG = "RegisterViewModel"
        private const val SUCCESS = "success"
    }

}