package com.simats.financetrack.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.financetrack.MainActivity
import com.simats.financetrack.R
import com.simats.financetrack.auth.AuthManager
import com.simats.financetrack.databinding.ActivityLoginBinding
import com.simats.financetrack.models.UserCredentials

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSignIn.setOnClickListener {
            attemptLogin()
        }

        binding.txtSignUp.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.txtForgotPassword.setOnClickListener {
            // TODO: Implement forgot password functionality
            Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun attemptLogin() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        // Validation
        if (email.isEmpty()) {
            binding.editTextEmail.error = "Email is required"
            return
        }

        if (password.isEmpty()) {
            binding.editTextPassword.error = "Password is required"
            return
        }

        if (!isValidEmail(email)) {
            binding.editTextEmail.error = "Please enter a valid email"
            return
        }

        // Show loading state
        binding.btnSignIn.isEnabled = false
        binding.btnSignIn.text = "Signing in..."

        // Attempt login
        val credentials = UserCredentials(email, password)
        val success = authManager.login(credentials)

        if (success) {
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            binding.btnSignIn.isEnabled = true
            binding.btnSignIn.text = "Sign In"
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onBackPressed() {
        // Prevent going back to previous screen
        moveTaskToBack(true)
    }
}

