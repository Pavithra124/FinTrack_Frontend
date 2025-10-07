package com.simats.financetrack.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.financetrack.R
import com.simats.financetrack.auth.AuthManager
import com.simats.financetrack.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnCreateAccount.setOnClickListener {
            attemptSignup()
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun attemptSignup() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Validation
        if (firstName.isEmpty()) {
            binding.etFirstName.error = "First name is required"
            return
        }

        if (lastName.isEmpty()) {
            binding.etLastName.error = "Last name is required"
            return
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            return
        }

        if (username.isEmpty()) {
            binding.etUsername.error = "Username is required"
            return
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            return
        }

        if (confirmPassword.isEmpty()) {
            binding.etConfirmPassword.error = "Please confirm your password"
            return
        }

        if (!isValidEmail(email)) {
            binding.etEmail.error = "Please enter a valid email"
            return
        }

        if (username.length < 3) {
            binding.etUsername.error = "Username must be at least 3 characters"
            return
        }

        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            return
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            return
        }

        if (!binding.cbTerms.isChecked) {
            Toast.makeText(this, "Please agree to the Terms and Privacy Policy", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading state
        binding.btnCreateAccount.isEnabled = false
        binding.btnCreateAccount.text = "Creating Account..."

        // Attempt registration
        val user = authManager.registerUser(username, email, password, firstName, lastName)

        if (user != null) {
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Username or email already exists", Toast.LENGTH_SHORT).show()
            binding.btnCreateAccount.isEnabled = true
            binding.btnCreateAccount.text = "Create Account"
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onBackPressed() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

