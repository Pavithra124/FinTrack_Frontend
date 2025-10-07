package com.simats.financetrack

import com.simats.financetrack.retrofit.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Your system IP with PHP project folder (dailyne)
    private const val BASE_URL = "https://8jkb7c9c-80.inc1.devtunnels.ms/FinanceTrack/"

    // Create Retrofit only once
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Expose ApiService instance
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
