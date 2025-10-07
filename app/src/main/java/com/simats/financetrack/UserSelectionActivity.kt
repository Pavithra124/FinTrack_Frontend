package com.simats.financetrack

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.simats.financetrack.auth.AuthManager
import com.simats.financetrack.models.User
import com.simats.financetrack.models.UserCredentials

class UserSelectionActivity : AppCompatActivity() {

    private lateinit var authManager: AuthManager
    private lateinit var userAdapter: UserSelectionAdapter
    private val users = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_selection)

        authManager = AuthManager(this)
        setupRecyclerView()
        setupUI()
        loadUsers()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewUsers)
        userAdapter = UserSelectionAdapter(users) { user ->
            showPasswordDialog(user)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = userAdapter
    }

    private fun setupUI() {
        val btnAddNewUser = findViewById<MaterialButton>(R.id.btnAddNewUser)
        btnAddNewUser.setOnClickListener {
            showCreateUserDialog()
        }
    }

    private fun loadUsers() {
        users.clear()
        users.addAll(authManager.getAllUsers())
        userAdapter.notifyDataSetChanged()

        // If no users exist, show create user dialog immediately
        if (users.isEmpty()) {
            showCreateUserDialog()
        }
    }

    private fun showPasswordDialog(user: User) {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_password_input, null)
        builder.setView(dialogView)

        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = dialogView.findViewById<MaterialButton>(R.id.btnLogin)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)

        builder.setTitle("Welcome back, ${user.displayName}")

        val dialog = builder.create()

        btnLogin.setOnClickListener {
            val password = etPassword.text.toString()

            if (password.isEmpty()) {
                etPassword.error = "Please enter your password"
                return@setOnClickListener
            }

            val credentials = UserCredentials(user.username, password)
            if (authManager.login(credentials)) {
                Toast.makeText(
                    this,
                    "Welcome back, ${user.displayName}!",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                dialog.dismiss()
            } else {
                etPassword.error = "Incorrect password"
                etPassword.text?.clear()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        etPassword.requestFocus()
    }

    private fun showCreateUserDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_user, null)
        builder.setView(dialogView)

        val etUsername = dialogView.findViewById<TextInputEditText>(R.id.etUsername)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = dialogView.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val etFirstName = dialogView.findViewById<TextInputEditText>(R.id.etFirstName)
        val etLastName = dialogView.findViewById<TextInputEditText>(R.id.etLastName)
        val btnCreate = dialogView.findViewById<MaterialButton>(R.id.btnCreate)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)

        builder.setTitle("Create New Account")

        val dialog = builder.create()

        btnCreate.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()

            // Validation
            when {
                username.isEmpty() -> {
                    etUsername.error = "Username is required"
                    return@setOnClickListener
                }
                username.length < 3 -> {
                    etUsername.error = "Username must be at least 3 characters"
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    etPassword.error = "Password is required"
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    etPassword.error = "Password must be at least 6 characters"
                    return@setOnClickListener
                }
                password != confirmPassword -> {
                    etConfirmPassword.error = "Passwords don't match"
                    return@setOnClickListener
                }
                email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    etEmail.error = "Please enter a valid email address"
                    return@setOnClickListener
                }
            }

            // Create user
            val newUser = authManager.registerUser(username, email, password, firstName, lastName)
            if (newUser != null) {
                Toast.makeText(
                    this,
                    "Account created successfully! Welcome, ${newUser.displayName}!",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Auto-login the new user
                authManager.login(newUser)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                dialog.dismiss()
            } else {
                Toast.makeText(
                    this,
                    "Username already exists. Please choose a different username.",
                    Toast.LENGTH_SHORT
                ).show()
                etUsername.error = "Username already exists"
            }
        }

        btnCancel.setOnClickListener {
            if (users.isEmpty()) {
                // If no users exist and user cancels, exit the app
                finish()
            } else {
                dialog.dismiss()
            }
        }

        dialog.show()
        etUsername.requestFocus()
    }

    override fun onBackPressed() {
        if (users.isEmpty()) {
            // If no users exist, exit the app
            finish()
        } else {
            super.onBackPressed()
        }
    }
}