package com.simats.financetrack

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.financetrack.auth.AuthManager
import com.simats.financetrack.databinding.ActivityWelcomeBinding
import com.simats.financetrack.models.User
import com.simats.financetrack.repository.ExpenseRepository

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var authManager: AuthManager
    private lateinit var repository: ExpenseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)
        repository = ExpenseRepository(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnGetStarted.setOnClickListener {
            // Redirect to signup
            startActivity(Intent(this, com.simats.financetrack.ui.auth.SignupActivity::class.java))
            finish()
        }

        binding.btnSkip.setOnClickListener {
            // Redirect to login
            startActivity(Intent(this, com.simats.financetrack.ui.auth.LoginActivity::class.java))
            finish()
        }
    }

}