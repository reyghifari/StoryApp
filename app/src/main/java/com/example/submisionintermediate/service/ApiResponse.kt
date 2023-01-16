package com.example.submisionintermediate.service

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)
