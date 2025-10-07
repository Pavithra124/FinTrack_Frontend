package com.simats.financetrack

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PersonalDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge content
        enableEdgeToEdge()

        // Set content layout
        setContentView(R.layout.activity_personal_details)

        // Insets padding for system bars
        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Example edit icon click listeners
        findViewById<ImageView>(R.id.ivEditFullName).setOnClickListener {
            Toast.makeText(this, "Edit Full Name clicked", Toast.LENGTH_SHORT).show()
            // Implement actual edit logic here
        }

        findViewById<ImageView>(R.id.ivEditDob).setOnClickListener {
            Toast.makeText(this, "Edit Date of Birth clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.ivEditEmail).setOnClickListener {
            Toast.makeText(this, "Edit Email clicked", Toast.LENGTH_SHORT).show()
        }

        // Logout Button → Go back to SignIn activity (home page)
        findViewById<Button>(R.id.btn_logout).setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Add more listeners for other edit icons similarly ...
    }
}
