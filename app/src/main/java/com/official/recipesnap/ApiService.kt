package com.official.recipesnap

import retrofit2.http.GET

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.Call

interface ApiService {
    @Multipart
    @POST("/upload")
    fun uploadImage(@Part image: MultipartBody.Part): Call<RecipeResponse>
}

data class RecipeResponse(val recipe: String)
