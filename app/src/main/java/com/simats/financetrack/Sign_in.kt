package com.simats.financetrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.financetrack.data.LoginRequest
import com.simats.financetrack.responses.LoginResponse
import com.simats.financetrack.retrofit.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignIn : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Redirect to new user selection flow
        startActivity(Intent(this, UserSelectionActivity::class.java))
        finish()
    }
    
    // Legacy method - keeping for compatibility but redirecting
    private fun legacyOnCreate() {
        setContentView(R.layout.activity_sign_in)

        val edtEmail = findViewById<EditText>(R.id.editTextEmail)
        val edtPassword = findViewById<EditText>(R.id.editTextPassword)
        val btnLogin = findViewById<Button>(R.id.btnSignIn)
        val txtSignUp = findViewById<TextView>(R.id.txtSignUp)

        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(email = email, password = password)

            ApiClient.apiService.checkUser(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val loginResponse = response.body()!!
                        if (loginResponse.success && loginResponse.user != null) {
                            Toast.makeText(
                                this@SignIn,
                                "Login Successful! Welcome ${loginResponse.user.name}",
                                Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(this@SignIn, FinancialOverview::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@SignIn,
                                "Login Failed: ${loginResponse.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@SignIn,
                            "Server Error: ${response.errorBody()?.string()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(
                        this@SignIn,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }

        txtSignUp.setOnClickListener {
            val intent = Intent(this, com.simats.financetrack.ui.auth.SignupActivity::class.java)
            startActivity(intent)
        }
    }
}
