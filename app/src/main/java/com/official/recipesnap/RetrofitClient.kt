package com.official.recipesnap

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.14:8080"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // Use this for local testing with emulator
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

}
