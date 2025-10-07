package com.simats.financetrack.responses

import com.simats.financetrack.retrofit.User

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null
)
