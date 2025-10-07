package com.simats.financetrack.retrofit

import com.simats.financetrack.data.LoginRequest
import com.simats.financetrack.responses.LoginResponse
import data.RegisterResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // Login endpoint
    @POST("login.php")
    fun checkUser(@Body request: LoginRequest): Call<LoginResponse>

    // Register endpoint
    @POST("signup.php")
    fun registerUser(@Body request: com.simats.financetrack.data.RegisterRequest): Call<RegisterResponse>
}
