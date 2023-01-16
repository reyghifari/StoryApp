package com.example.submisionintermediate.service

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") pass: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") pass: String
    ): Call<LoginResponse>

    @GET("/v1/stories")
    suspend fun getStoryPaging(
        @Header("Authorization") authHeader: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ):  StoriesResponse

    @Multipart
    @POST("/v1/stories")
    fun uploadImage( //add story without location
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Header("Authorization") token: String
    ): Call<ApiResponse>

    @Multipart
    @POST("stories")
    fun addStory(
        @Header("Authorization") authHeader: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: Double,
        @Part("lon") lon: Double
    ): Call<ApiResponse>

    @GET("/v1/stories")
    fun getAllStoriesMap(
        @Header("Authorization") authHeader: String,
        @Query("location")value: String
    ): Call<StoriesResponse>

    @GET("stories")
    fun getAllStories(
        @Header("Authorization") token: String
    ): Call<StoriesResponse>
}